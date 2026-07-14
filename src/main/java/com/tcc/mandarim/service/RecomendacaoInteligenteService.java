package com.tcc.mandarim.service;

import com.tcc.mandarim.entity.*;
import com.tcc.mandarim.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecomendacaoInteligenteService {

    private final UsuarioRepository usuarioRepository;
    private final ConteudoRepository conteudoRepository;
    private final ExercicioRepository exercicioRepository;
    private final RespostaRepository respostaRepository;
    private final RevisaoRepository revisaoRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> gerarPlanoInteligente(UUID usuarioId) {
        Map<String, Object> plano = new LinkedHashMap<>();

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) return plano;

        List<Resposta> respostas = respostaRepository.findByUsuarioId(usuarioId);
        List<Revisao> revisoes = revisaoRepository.findByUsuarioId(usuarioId);
        List<Conteudo> todosConteudos = conteudoRepository.findAll();
        LocalDate hoje = LocalDate.now();

        // === Cálculos base ===
        long totalRespostas = respostas.size();
        long acertos = respostas.stream().filter(r -> Boolean.TRUE.equals(r.getCorreta())).count();
        double taxaAcerto = totalRespostas > 0 ? (acertos * 100.0 / totalRespostas) : 0.0;

        double tempoMedio = respostas.stream()
                .filter(r -> r.getTempoRespostaSegundos() != null)
                .mapToInt(Resposta::getTempoRespostaSegundos)
                .average().orElse(0.0);

        long revisoesPendentes = revisoes.stream()
                .filter(r -> r.getProximaRevisao() != null && !r.getProximaRevisao().isAfter(hoje))
                .count();

        long conteudosCriticos = revisoes.stream()
                .filter(r -> r.getLapsos() >= 2).count();

        // Tema mais crítico (com mais erros)
        Map<String, Long> errosPorTema = new LinkedHashMap<>();
        respostas.stream()
                .filter(r -> Boolean.FALSE.equals(r.getCorreta()))
                .forEach(r -> {
                    try {
                        Exercicio ex = exercicioRepository.findById(r.getExercicioId()).orElse(null);
                        if (ex != null && ex.getConteudo() != null && ex.getConteudo().getTemas() != null) {
                            ex.getConteudo().getTemas().forEach(t ->
                                    errosPorTema.merge(t.getNome(), 1L, Long::sum));
                        }
                    } catch (Exception ignored) {}
                });

        String temaMaisCritico = errosPorTema.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Nenhum identificado");

        // Conteúdos já estudados
        Set<Long> conteudosEstudados = respostas.stream()
                .map(r -> {
                    try {
                        Exercicio ex = exercicioRepository.findById(r.getExercicioId()).orElse(null);
                        return ex != null && ex.getConteudo() != null ? ex.getConteudo().getId() : null;
                    } catch (Exception e) { return null; }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Conteúdos novos sugeridos (que o aluno ainda não estudou)
        List<Conteudo> conteudosNovos = todosConteudos.stream()
                .filter(c -> !conteudosEstudados.contains(c.getId()))
                .limit(5)
                .collect(Collectors.toList());

        // === KPIs do card superior ===
        // Próxima melhor ação
        String proximaAcao;
        if (revisoesPendentes > 3) {
            proximaAcao = "Revisar " + revisoesPendentes + " conteúdos pendentes";
        } else if (conteudosCriticos > 0) {
            proximaAcao = "Reforçar " + conteudosCriticos + " conteúdos críticos";
        } else if (conteudosNovos.size() > 0) {
            proximaAcao = "Estudar conteúdos novos de HSK " + usuario.getNivelHskAtual();
        } else {
            proximaAcao = "Praticar exercícios para manter retenção";
        }

        int exerciciosRecomendados = (int) Math.min(10, revisoesPendentes * 2 + 3);
        int tempoEstimado = exerciciosRecomendados * 2 + (int) revisoesPendentes + conteudosNovos.size() * 3;
        double impactoRetencao = Math.min(15.0, revisoesPendentes * 2 + conteudosCriticos * 3);

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("proximaAcao", proximaAcao);
        kpis.put("temaMaisCritico", temaMaisCritico);
        kpis.put("conteudosSugeridos", conteudosNovos.size() + revisoesPendentes);
        kpis.put("exerciciosRecomendados", exerciciosRecomendados);
        kpis.put("tempoEstimadoMinutos", tempoEstimado);
        kpis.put("impactoRetencao", "+" + Math.round(impactoRetencao * 10.0) / 10.0 + "%");
        plano.put("kpis", kpis);

        // === Lista de Recomendações ===
        List<Map<String, Object>> recomendacoes = new ArrayList<>();

        // 1. Revisões pendentes (tipo REVISAO)
        revisoes.stream()
                .filter(r -> r.getProximaRevisao() != null && !r.getProximaRevisao().isAfter(hoje))
                .sorted(Comparator.comparingInt(Revisao::getLapsos).reversed())
                .limit(5)
                .forEach(rev -> {
                    Conteudo c = rev.getConteudo() != null ? rev.getConteudo()
                            : conteudoRepository.findById(rev.getConteudoId()).orElse(null);
                    if (c == null) return;

                    long diasSemRevisao = rev.getUltimaRevisao() != null
                            ? ChronoUnit.DAYS.between(rev.getUltimaRevisao().toLocalDate(), hoje) : 30;

                    Map<String, Object> rec = new LinkedHashMap<>();
                    rec.put("conteudoId", c.getId());
                    rec.put("hanzi", c.getHanzi());
                    rec.put("pinyin", c.getPinyin());
                    rec.put("traducao", c.getTraducao());
                    rec.put("tipo", "REVISAO");
                    rec.put("prioridade", rev.getLapsos() >= 2 ? "ALTA" : "MEDIA");
                    rec.put("score", Math.min(100, 40 + rev.getLapsos() * 15 + (int) diasSemRevisao));
                    rec.put("motivo", gerarMotivoRecomendacao(rev, diasSemRevisao));
                    rec.put("impacto", "Melhora retenção em ~" + (rev.getLapsos() + 1) * 3 + "%");
                    rec.put("taxaAcerto", calcularTaxaConteudo(c.getId(), respostas));
                    rec.put("tempoMedio", calcularTempoConteudo(c.getId(), respostas));
                    rec.put("lapsos", rev.getLapsos());
                    rec.put("nivelHsk", c.getNivelHsk());
                    recomendacoes.add(rec);
                });

        // 2. Conteúdos com baixa taxa de acerto (tipo EXERCICIO)
        revisoes.stream()
                .filter(r -> r.getConteudo() != null || conteudoRepository.findById(r.getConteudoId()).isPresent())
                .forEach(rev -> {
                    Conteudo c = rev.getConteudo() != null ? rev.getConteudo()
                            : conteudoRepository.findById(rev.getConteudoId()).orElse(null);
                    if (c == null) return;
                    double taxa = calcularTaxaConteudo(c.getId(), respostas);
                    if (taxa > 0 && taxa < 70 && recomendacoes.stream()
                            .noneMatch(r -> c.getId().equals(r.get("conteudoId")))) {
                        Map<String, Object> rec = new LinkedHashMap<>();
                        rec.put("conteudoId", c.getId());
                        rec.put("hanzi", c.getHanzi());
                        rec.put("pinyin", c.getPinyin());
                        rec.put("traducao", c.getTraducao());
                        rec.put("tipo", "EXERCICIO");
                        rec.put("prioridade", taxa < 50 ? "ALTA" : "MEDIA");
                        rec.put("score", (int)(100 - taxa));
                        rec.put("motivo", String.format("Taxa de acerto de %.0f%% — pratique mais exercícios.", taxa));
                        rec.put("impacto", "Aumentar domínio em ~10%");
                        rec.put("taxaAcerto", taxa);
                        rec.put("lapsos", rev.getLapsos());
                        rec.put("nivelHsk", c.getNivelHsk());
                        recomendacoes.add(rec);
                    }
                });

        // 3. Conteúdos novos (tipo CONTEUDO_NOVO)
        for (Conteudo c : conteudosNovos) {
            Map<String, Object> rec = new LinkedHashMap<>();
            rec.put("conteudoId", c.getId());
            rec.put("hanzi", c.getHanzi());
            rec.put("pinyin", c.getPinyin());
            rec.put("traducao", c.getTraducao());
            rec.put("tipo", "CONTEUDO_NOVO");
            rec.put("prioridade", "BAIXA");
            rec.put("score", 30);
            rec.put("motivo", "Conteúdo novo — expandir vocabulário HSK " + c.getNivelHsk());
            rec.put("impacto", "Expandir vocabulário");
            rec.put("nivelHsk", c.getNivelHsk());
            recomendacoes.add(rec);
        }

        // 4. Sugestão de IA (tipo IA)
        Map<String, Object> recIA = new LinkedHashMap<>();
        recIA.put("tipo", "IA");
        recIA.put("prioridade", "MEDIA");
        recIA.put("score", 50);
        recIA.put("motivo", "Gerar conteúdo personalizado com IA para reforçar tema: " + temaMaisCritico);
        recIA.put("impacto", "Conteúdo personalizado para suas dificuldades");
        recIA.put("hanzi", "🤖");
        recIA.put("pinyin", "IA");
        recIA.put("traducao", "Gerar conteúdo com Inteligência Artificial");
        recomendacoes.add(recIA);

        // Ordena por score
        recomendacoes.sort((a, b) -> Integer.compare(
                (int) b.getOrDefault("score", 0),
                (int) a.getOrDefault("score", 0)));

        plano.put("recomendacoes", recomendacoes);

        // === Plano de Estudo Sugerido ===
        Map<String, Object> planoEstudo = new LinkedHashMap<>();
        planoEstudo.put("revisarCriticos", Math.min(5, revisoesPendentes));
        planoEstudo.put("praticarExercicios", exerciciosRecomendados);
        planoEstudo.put("conteudosNovos", Math.min(3, conteudosNovos.size()));
        planoEstudo.put("reforcarTema", temaMaisCritico);
        planoEstudo.put("tempoTotal", tempoEstimado);
        plano.put("planoEstudo", planoEstudo);

        // === Insights ===
        List<String> insights = new ArrayList<>();
        if (revisoesPendentes > 0) {
            insights.add("Você tem " + revisoesPendentes + " revisões pendentes. Comece por elas.");
        }
        if (conteudosCriticos > 0) {
            insights.add("Existem " + conteudosCriticos + " conteúdos críticos com lapsos recorrentes.");
        }
        if (taxaAcerto < 70) {
            insights.add("Sua taxa de acerto está em " + Math.round(taxaAcerto) + "%. Pratique mais.");
        }
        if (tempoMedio > 10) {
            insights.add("Seu tempo médio de resposta está alto (" + Math.round(tempoMedio) + "s). Tente ser mais rápido.");
        }
        if (!temaMaisCritico.equals("Nenhum identificado")) {
            insights.add("O tema \"" + temaMaisCritico + "\" concentra a maior parte dos seus erros.");
        }
        if (conteudosNovos.size() > 0) {
            insights.add("Há " + conteudosNovos.size() + " conteúdos novos disponíveis para expandir seu vocabulário.");
        }
        plano.put("insights", insights);

        return plano;
    }

    // === Helpers ===

    private String gerarMotivoRecomendacao(Revisao rev, long diasSemRevisao) {
        List<String> motivos = new ArrayList<>();
        if (rev.getProximaRevisao() != null && !rev.getProximaRevisao().isAfter(LocalDate.now())) {
            motivos.add("Revisão vencida");
        }
        if (rev.getLapsos() >= 2) {
            motivos.add(rev.getLapsos() + " lapsos recorrentes");
        }
        if (diasSemRevisao > 5) {
            motivos.add("Há " + diasSemRevisao + " dias sem revisão");
        }
        return motivos.isEmpty() ? "Revisão recomendada" : String.join(". ", motivos) + ".";
    }

    private double calcularTaxaConteudo(Long conteudoId, List<Resposta> respostas) {
        List<Long> exercicioIds = exercicioRepository.findByConteudoId(conteudoId)
                .stream().map(Exercicio::getId).collect(Collectors.toList());
        List<Resposta> respostasConteudo = respostas.stream()
                .filter(r -> exercicioIds.contains(r.getExercicioId()))
                .collect(Collectors.toList());
        if (respostasConteudo.isEmpty()) return 0.0;
        long acertos = respostasConteudo.stream().filter(r -> Boolean.TRUE.equals(r.getCorreta())).count();
        return Math.round((acertos * 100.0 / respostasConteudo.size()) * 10.0) / 10.0;
    }

    private double calcularTempoConteudo(Long conteudoId, List<Resposta> respostas) {
        List<Long> exercicioIds = exercicioRepository.findByConteudoId(conteudoId)
                .stream().map(Exercicio::getId).collect(Collectors.toList());
        return respostas.stream()
                .filter(r -> exercicioIds.contains(r.getExercicioId()))
                .filter(r -> r.getTempoRespostaSegundos() != null)
                .mapToInt(Resposta::getTempoRespostaSegundos)
                .average().orElse(0.0);
    }
}
