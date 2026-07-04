package com.tcc.mandarim.service;

import com.tcc.mandarim.dto.request.RegistrarRespostaRevisaoRequest;
import com.tcc.mandarim.dto.response.IndicadoresRevisaoResponse;
import com.tcc.mandarim.dto.response.IndicadoresRevisaoResponse.AcertosErros;
import com.tcc.mandarim.dto.response.IndicadoresRevisaoResponse.EvolucaoDiaria;
import com.tcc.mandarim.dto.response.RevisaoInteligenteResponse;
import com.tcc.mandarim.entity.Conteudo;
import com.tcc.mandarim.entity.Resposta;
import com.tcc.mandarim.entity.Revisao;
import com.tcc.mandarim.exception.ResourceNotFoundException;
import com.tcc.mandarim.repository.ConteudoRepository;
import com.tcc.mandarim.repository.ExercicioRepository;
import com.tcc.mandarim.repository.RespostaRepository;
import com.tcc.mandarim.repository.RevisaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevisaoInteligenteService {

    private final RevisaoRepository revisaoRepository;
    private final RespostaRepository respostaRepository;
    private final ConteudoRepository conteudoRepository;
    private final ExercicioRepository exercicioRepository;

    // ─── Revisões Inteligentes (priorizadas) ─────────────────────────────────

    @Transactional(readOnly = true)
    public List<RevisaoInteligenteResponse> buscarRevisoesInteligentes(UUID usuarioId) {
        List<Revisao> todasRevisoes = revisaoRepository.findByUsuarioId(usuarioId);
        List<Resposta> todasRespostas = respostaRepository.findByUsuarioId(usuarioId);

        // Calcula tempo médio do usuário para comparação
        double tempoMedioUsuario = todasRespostas.stream()
                .filter(r -> r.getTempoRespostaSegundos() != null)
                .mapToInt(Resposta::getTempoRespostaSegundos)
                .average()
                .orElse(10.0);

        List<RevisaoInteligenteResponse> resultado = new ArrayList<>();

        for (Revisao revisao : todasRevisoes) {
            Conteudo conteudo = revisao.getConteudo();
            if (conteudo == null) {
                conteudo = conteudoRepository.findById(revisao.getConteudoId()).orElse(null);
            }
            if (conteudo == null) continue;

            // Busca respostas relacionadas a este conteúdo
            List<Long> exercicioIds = exercicioRepository.findByConteudoId(revisao.getConteudoId())
                    .stream().map(e -> e.getId()).collect(Collectors.toList());

            List<Resposta> respostasConteudo = todasRespostas.stream()
                    .filter(r -> exercicioIds.contains(r.getExercicioId()))
                    .collect(Collectors.toList());

            // Taxa de acerto do conteúdo
            long totalRespostas = respostasConteudo.size();
            long acertos = respostasConteudo.stream().filter(r -> Boolean.TRUE.equals(r.getCorreta())).count();
            double taxaAcerto = totalRespostas > 0 ? (acertos * 100.0 / totalRespostas) : 50.0;

            // Tempo médio de resposta para este conteúdo
            double tempoMedioConteudo = respostasConteudo.stream()
                    .filter(r -> r.getTempoRespostaSegundos() != null)
                    .mapToInt(Resposta::getTempoRespostaSegundos)
                    .average()
                    .orElse(0.0);

            // Dias desde última revisão
            long diasDesdeUltimaRevisao = 0;
            if (revisao.getUltimaRevisao() != null) {
                diasDesdeUltimaRevisao = ChronoUnit.DAYS.between(
                        revisao.getUltimaRevisao().toLocalDate(), LocalDate.now());
            } else {
                diasDesdeUltimaRevisao = 30; // nunca revisado = alta prioridade
            }

            // Probabilidade de esquecimento
            double probabilidadeEsquecimento = calcularProbabilidadeEsquecimento(
                    diasDesdeUltimaRevisao, revisao.getIntervaloDias(), revisao.getLapsos());

            // Score de prioridade
            int score = calcularScorePrioridade(
                    revisao, taxaAcerto, tempoMedioConteudo, tempoMedioUsuario, diasDesdeUltimaRevisao);

            // Classificação
            String prioridade = score >= 70 ? "ALTA" : score >= 40 ? "MEDIA" : "BAIXA";

            // Motivo da recomendação
            String motivo = gerarMotivo(revisao, taxaAcerto, tempoMedioConteudo,
                    tempoMedioUsuario, diasDesdeUltimaRevisao);

            // Tema (do primeiro tema associado ao conteúdo)
            String tema = null;
            if (conteudo.getTemas() != null && !conteudo.getTemas().isEmpty()) {
                tema = conteudo.getTemas().iterator().next().getNome();
            }

            resultado.add(RevisaoInteligenteResponse.builder()
                    .revisaoId(revisao.getId())
                    .conteudoId(conteudo.getId())
                    .hanzi(conteudo.getHanzi())
                    .pinyin(conteudo.getPinyin())
                    .traducao(conteudo.getTraducao())
                    .nivelHsk(conteudo.getNivelHsk())
                    .tema(tema)
                    .ultimaRevisao(revisao.getUltimaRevisao() != null
                            ? revisao.getUltimaRevisao().toLocalDate().toString() : null)
                    .proximaRevisao(revisao.getProximaRevisao() != null
                            ? revisao.getProximaRevisao().toString() : null)
                    .repeticoes(revisao.getRepeticoes())
                    .lapsos(revisao.getLapsos())
                    .taxaAcerto(Math.round(taxaAcerto * 100.0) / 100.0)
                    .tempoMedioResposta(Math.round(tempoMedioConteudo * 10.0) / 10.0)
                    .probabilidadeEsquecimento(Math.round(probabilidadeEsquecimento * 10.0) / 10.0)
                    .scorePrioridade(score)
                    .prioridade(prioridade)
                    .motivo(motivo)
                    .build());
        }

        // Ordena por score descrescente (alta prioridade primeiro)
        resultado.sort(Comparator.comparingInt(RevisaoInteligenteResponse::getScorePrioridade).reversed());

        return resultado;
    }

    // ─── Indicadores para BI ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public IndicadoresRevisaoResponse calcularIndicadores(UUID usuarioId) {
        List<Revisao> revisoes = revisaoRepository.findByUsuarioId(usuarioId);
        List<Resposta> respostas = respostaRepository.findByUsuarioId(usuarioId);

        // Revisões pendentes hoje
        int pendentes = (int) revisoes.stream()
                .filter(r -> r.getProximaRevisao() != null && !r.getProximaRevisao().isAfter(LocalDate.now()))
                .count();

        // Taxa de acerto em revisões (respostas dos últimos 30 dias)
        LocalDateTime limite30Dias = LocalDateTime.now().minusDays(30);
        List<Resposta> respostasRecentes = respostas.stream()
                .filter(r -> r.getRespondidoEm() != null && r.getRespondidoEm().isAfter(limite30Dias))
                .collect(Collectors.toList());

        long totalRecentes = respostasRecentes.size();
        long acertosRecentes = respostasRecentes.stream()
                .filter(r -> Boolean.TRUE.equals(r.getCorreta())).count();
        double taxaAcertoRevisoes = totalRecentes > 0 ? (acertosRecentes * 100.0 / totalRecentes) : 0.0;

        // Conteúdos críticos (lapsos >= 2 ou taxa acerto < 70%)
        int criticos = 0;
        for (Revisao rev : revisoes) {
            if (rev.getLapsos() >= 2) {
                criticos++;
            }
        }

        // Tempo médio de resposta
        double tempoMedio = respostas.stream()
                .filter(r -> r.getTempoRespostaSegundos() != null)
                .mapToInt(Resposta::getTempoRespostaSegundos)
                .average()
                .orElse(0.0);

        // Taxa de retenção estimada (conteúdos com taxa >= 80% / total)
        int dominados = (int) revisoes.stream()
                .filter(r -> r.getRepeticoes() >= 3 && r.getLapsos() <= 1)
                .count();

        int emAprendizado = revisoes.size() - dominados;

        double taxaRetencao = revisoes.isEmpty() ? 0.0
                : (dominados * 100.0 / revisoes.size());

        // Evolução da retenção nos últimos 14 dias
        List<EvolucaoDiaria> evolucao = calcularEvolucaoRetencao(respostas, 14);

        // Revisões por prioridade
        List<RevisaoInteligenteResponse> inteligentes = buscarRevisoesInteligentes(usuarioId);
        Map<String, Integer> porPrioridade = new LinkedHashMap<>();
        porPrioridade.put("ALTA", (int) inteligentes.stream().filter(r -> "ALTA".equals(r.getPrioridade())).count());
        porPrioridade.put("MEDIA", (int) inteligentes.stream().filter(r -> "MEDIA".equals(r.getPrioridade())).count());
        porPrioridade.put("BAIXA", (int) inteligentes.stream().filter(r -> "BAIXA".equals(r.getPrioridade())).count());

        // Erros por tema
        Map<String, Integer> errosPorTema = calcularErrosPorTema(respostas);

        // Acertos vs erros total
        long totalAcertos = respostas.stream().filter(r -> Boolean.TRUE.equals(r.getCorreta())).count();
        long totalErros = respostas.stream().filter(r -> Boolean.FALSE.equals(r.getCorreta())).count();

        return IndicadoresRevisaoResponse.builder()
                .revisoesPendentes(pendentes)
                .conteudosCriticos(criticos)
                .taxaRetencao(Math.round(taxaRetencao * 10.0) / 10.0)
                .taxaAcertoRevisoes(Math.round(taxaAcertoRevisoes * 10.0) / 10.0)
                .tempoMedioResposta(Math.round(tempoMedio * 10.0) / 10.0)
                .conteudosDominados(dominados)
                .conteudosEmAprendizado(emAprendizado)
                .evolucaoRetencao(evolucao)
                .revisoesPorPrioridade(porPrioridade)
                .errosPorTema(errosPorTema)
                .acertosVsErros(AcertosErros.builder().acertos(totalAcertos).erros(totalErros).build())
                .build();
    }

    // ─── Registrar resposta de revisão ───────────────────────────────────────

    @Transactional
    public RevisaoInteligenteResponse registrarResposta(RegistrarRespostaRevisaoRequest request) {
        Revisao revisao = revisaoRepository.findById(request.getRevisaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Revisão", request.getRevisaoId()));

        Conteudo conteudo = conteudoRepository.findById(revisao.getConteudoId())
                .orElseThrow(() -> new ResourceNotFoundException("Conteúdo", revisao.getConteudoId()));

        // Verifica se resposta está correta (comparação case-insensitive com a tradução)
        String respostaUsuario = request.getRespostaUsuario().trim().toLowerCase();
        String respostaEsperada = conteudo.getTraducao().trim().toLowerCase();
        boolean correta = respostaUsuario.equals(respostaEsperada)
                || respostaEsperada.contains(respostaUsuario)
                || respostaUsuario.contains(respostaEsperada);

        // Salva a resposta na tabela respostas
        // Busca um exercício associado a este conteúdo para vincular
        Long exercicioId = exercicioRepository.findByConteudoId(conteudo.getId())
                .stream().findFirst().map(e -> e.getId()).orElse(null);

        if (exercicioId != null) {
            Resposta resposta = Resposta.builder()
                    .usuarioId(request.getUsuarioId())
                    .exercicioId(exercicioId)
                    .respostaUsuario(request.getRespostaUsuario())
                    .correta(correta)
                    .tempoRespostaSegundos(request.getTempoRespostaSegundos())
                    .build();
            respostaRepository.save(resposta);
        }

        // Atualiza revisão espaçada
        atualizarRevisaoEspacada(revisao, correta, request.getTempoRespostaSegundos());

        // Retorna resposta atualizada
        log.info("[Revisão] Resposta registrada para revisao={}, conteudo='{}', correta={}",
                revisao.getId(), conteudo.getHanzi(), correta);

        // Recalcula score atualizado
        return RevisaoInteligenteResponse.builder()
                .revisaoId(revisao.getId())
                .conteudoId(conteudo.getId())
                .hanzi(conteudo.getHanzi())
                .pinyin(conteudo.getPinyin())
                .traducao(conteudo.getTraducao())
                .nivelHsk(conteudo.getNivelHsk())
                .proximaRevisao(revisao.getProximaRevisao().toString())
                .repeticoes(revisao.getRepeticoes())
                .lapsos(revisao.getLapsos())
                .taxaAcerto(correta ? 100.0 : 0.0)
                .prioridade(correta ? "BAIXA" : "ALTA")
                .motivo(correta ? "Resposta correta! Próxima revisão agendada." : "Resposta incorreta. Revisão agendada para amanhã.")
                .build();
    }

    // ─── Lógica de Revisão Espaçada Aprimorada ───────────────────────────────

    private void atualizarRevisaoEspacada(Revisao revisao, boolean correta, Integer tempoResposta) {
        double facilidade = revisao.getFacilidade().doubleValue();
        int repeticoes = revisao.getRepeticoes();
        int intervaloDias = revisao.getIntervaloDias();
        int lapsos = revisao.getLapsos();

        // Determina se foi resposta rápida (abaixo da média)
        boolean respostaRapida = tempoResposta != null && tempoResposta < 8;

        if (correta) {
            repeticoes++;
            if (respostaRapida) {
                // Acertou rápido: aumentar intervalo bastante
                facilidade += 0.1;
                intervaloDias = Math.max(1, (int) Math.round(intervaloDias * facilidade));
            } else {
                // Acertou mas demorou: manter ou aumentar pouco
                facilidade += 0.05;
                intervaloDias = Math.max(1, (int) Math.round(intervaloDias * 1.2));
            }
        } else {
            // Errou: reduzir intervalo e aumentar lapsos
            lapsos++;
            facilidade = Math.max(1.3, facilidade - 0.2);
            intervaloDias = 1;
        }

        revisao.setFacilidade(BigDecimal.valueOf(facilidade).setScale(2, RoundingMode.HALF_UP));
        revisao.setRepeticoes(repeticoes);
        revisao.setIntervaloDias(intervaloDias);
        revisao.setLapsos(lapsos);
        revisao.setUltimaRevisao(LocalDateTime.now());
        revisao.setProximaRevisao(LocalDate.now().plusDays(intervaloDias));
        revisao.setAtualizadoEm(LocalDateTime.now());

        revisaoRepository.save(revisao);
    }

    // ─── Cálculos de Prioridade ──────────────────────────────────────────────

    private int calcularScorePrioridade(Revisao revisao, double taxaAcerto,
                                         double tempoMedioConteudo, double tempoMedioUsuario,
                                         long diasDesdeUltimaRevisao) {
        int score = 0;

        // +40 se proxima_revisao <= hoje (vencida)
        if (revisao.getProximaRevisao() != null && !revisao.getProximaRevisao().isAfter(LocalDate.now())) {
            score += 40;
        }

        // +25 se taxa de acerto < 70%
        if (taxaAcerto < 70.0) {
            score += 25;
        }

        // +20 se lapsos >= 2
        if (revisao.getLapsos() >= 2) {
            score += 20;
        }

        // +15 se tempo médio de resposta acima da média do usuário
        if (tempoMedioConteudo > tempoMedioUsuario && tempoMedioConteudo > 0) {
            score += 15;
        }

        // +10 se dias desde última revisão > intervalo
        if (diasDesdeUltimaRevisao > revisao.getIntervaloDias()) {
            score += 10;
        }

        return Math.min(100, score);
    }

    private double calcularProbabilidadeEsquecimento(long diasDesdeUltimaRevisao,
                                                      int intervaloDias, int lapsos) {
        if (intervaloDias <= 0) intervaloDias = 1;
        double prob = (diasDesdeUltimaRevisao * 50.0 / intervaloDias) + (lapsos * 10.0);
        return Math.min(100.0, Math.max(0.0, prob));
    }

    private String gerarMotivo(Revisao revisao, double taxaAcerto,
                               double tempoMedioConteudo, double tempoMedioUsuario,
                               long diasDesdeUltimaRevisao) {
        List<String> motivos = new ArrayList<>();

        if (revisao.getProximaRevisao() != null && !revisao.getProximaRevisao().isAfter(LocalDate.now())) {
            motivos.add("Conteúdo vencido para revisão");
        }

        if (taxaAcerto < 70.0 && taxaAcerto > 0) {
            motivos.add(String.format("Taxa de acerto abaixo de 70%% (%.0f%%)", taxaAcerto));
        }

        if (revisao.getLapsos() >= 2) {
            motivos.add(String.format("Você errou este conteúdo %d vezes", revisao.getLapsos()));
        }

        if (tempoMedioConteudo > tempoMedioUsuario && tempoMedioConteudo > 0) {
            motivos.add("Tempo médio de resposta acima da sua média");
        }

        if (diasDesdeUltimaRevisao > 7) {
            motivos.add(String.format("Está há %d dias sem revisão", diasDesdeUltimaRevisao));
        }

        if (motivos.isEmpty()) {
            motivos.add("Revisão de manutenção programada");
        }

        return String.join(". ", motivos) + ".";
    }

    // ─── Helpers para Gráficos ───────────────────────────────────────────────

    private List<EvolucaoDiaria> calcularEvolucaoRetencao(List<Resposta> respostas, int dias) {
        List<EvolucaoDiaria> evolucao = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

        for (int i = dias - 1; i >= 0; i--) {
            LocalDate data = LocalDate.now().minusDays(i);
            LocalDateTime inicioDia = data.atStartOfDay();
            LocalDateTime fimDia = data.plusDays(1).atStartOfDay();

            List<Resposta> doDia = respostas.stream()
                    .filter(r -> r.getRespondidoEm() != null
                            && r.getRespondidoEm().isAfter(inicioDia)
                            && r.getRespondidoEm().isBefore(fimDia))
                    .collect(Collectors.toList());

            int totalDia = doDia.size();
            long acertosDia = doDia.stream().filter(r -> Boolean.TRUE.equals(r.getCorreta())).count();
            double taxa = totalDia > 0 ? (acertosDia * 100.0 / totalDia) : 0.0;

            evolucao.add(EvolucaoDiaria.builder()
                    .data(data.format(fmt))
                    .taxaRetencao(Math.round(taxa * 10.0) / 10.0)
                    .revisoes(totalDia)
                    .build());
        }

        return evolucao;
    }

    private Map<String, Integer> calcularErrosPorTema(List<Resposta> respostas) {
        Map<String, Integer> errosPorTema = new LinkedHashMap<>();

        List<Resposta> erros = respostas.stream()
                .filter(r -> Boolean.FALSE.equals(r.getCorreta()))
                .collect(Collectors.toList());

        for (Resposta erro : erros) {
            // Busca conteúdo via exercício
            exercicioRepository.findById(erro.getExercicioId()).ifPresent(exercicio -> {
                Conteudo conteudo = exercicio.getConteudo();
                if (conteudo != null && conteudo.getTemas() != null) {
                    for (var tema : conteudo.getTemas()) {
                        errosPorTema.merge(tema.getNome(), 1, Integer::sum);
                    }
                }
                if (conteudo != null && (conteudo.getTemas() == null || conteudo.getTemas().isEmpty())) {
                    errosPorTema.merge("Sem tema", 1, Integer::sum);
                }
            });
        }

        return errosPorTema;
    }
}
