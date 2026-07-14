package com.tcc.mandarim.service;

import com.tcc.mandarim.entity.Conteudo;
import com.tcc.mandarim.entity.ConteudoIa;
import com.tcc.mandarim.entity.Resposta;
import com.tcc.mandarim.entity.Revisao;
import com.tcc.mandarim.entity.Usuario;
import com.tcc.mandarim.entity.enums.OrigemConteudo;
import com.tcc.mandarim.entity.enums.StatusIA;
import com.tcc.mandarim.repository.ConteudoIaRepository;
import com.tcc.mandarim.repository.ConteudoRepository;
import com.tcc.mandarim.repository.ExercicioRepository;
import com.tcc.mandarim.repository.RespostaRepository;
import com.tcc.mandarim.repository.RevisaoRepository;
import com.tcc.mandarim.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UsuarioRepository usuarioRepository;
    private final ConteudoRepository conteudoRepository;
    private final ExercicioRepository exercicioRepository;
    private final RespostaRepository respostaRepository;
    private final RevisaoRepository revisaoRepository;
    private final ConteudoIaRepository conteudoIaRepository;

    public Map<String, Object> adminDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();

        List<Usuario> todosUsuarios = usuarioRepository.findAll();
        List<Resposta> todasRespostas = respostaRepository.findAll();
        List<Revisao> todasRevisoes = revisaoRepository.findAll();
        List<Conteudo> todosConteudos = conteudoRepository.findAll();
        List<ConteudoIa> todosConteudosIa = conteudoIaRepository.findAll();
        LocalDate hoje = LocalDate.now();

        // === KPIs ===
        long totalUsuarios = todosUsuarios.size();
        long usuariosAtivos = todosUsuarios.stream().filter(u -> Boolean.TRUE.equals(u.getAtivo())).count();
        long totalConteudos = conteudoRepository.count();
        long totalExercicios = exercicioRepository.count();
        long conteudosGeradosIA = todosConteudosIa.size();
        long totalRespostas = todasRespostas.size();
        long revisoesRealizadas = todasRevisoes.stream()
                .filter(r -> r.getUltimaRevisao() != null)
                .count();
        long revisoesPendentesTotal = todasRevisoes.stream()
                .filter(r -> r.getProximaRevisao() != null && !r.getProximaRevisao().isAfter(hoje))
                .count();

        long acertos = todasRespostas.stream().filter(r -> Boolean.TRUE.equals(r.getCorreta())).count();
        double mediaGeralAcerto = totalRespostas > 0
                ? Math.round((acertos * 100.0 / totalRespostas) * 10.0) / 10.0
                : 0.0;

        long revisoesComRepeticoesAltas = todasRevisoes.stream()
                .filter(r -> r.getRepeticoes() >= 3 && r.getLapsos() <= 1)
                .count();
        long totalRevisoes = todasRevisoes.size();
        double mediaRetencao = totalRevisoes > 0
                ? Math.round((revisoesComRepeticoesAltas * 100.0 / totalRevisoes) * 10.0) / 10.0
                : 0.0;

        double tempoMedioResposta = todasRespostas.stream()
                .filter(r -> r.getTempoRespostaSegundos() != null)
                .mapToDouble(r -> r.getTempoRespostaSegundos().doubleValue())
                .average()
                .orElse(0.0);
        tempoMedioResposta = Math.round(tempoMedioResposta * 10.0) / 10.0;

        long conteudosDominados = revisoesComRepeticoesAltas;
        long conteudosCriticos = todasRevisoes.stream()
                .filter(r -> r.getLapsos() >= 2)
                .count();

        double probabilidadeMediaEsquecimento = todasRevisoes.stream()
                .mapToDouble(this::calcularProbabilidadeEsquecimento)
                .average()
                .orElse(0.0);
        probabilidadeMediaEsquecimento = Math.round(probabilidadeMediaEsquecimento * 10.0) / 10.0;

        dashboard.put("totalUsuarios", totalUsuarios);
        dashboard.put("usuariosAtivos", usuariosAtivos);
        dashboard.put("totalConteudos", totalConteudos);
        dashboard.put("totalExercicios", totalExercicios);
        dashboard.put("conteudosGeradosIA", conteudosGeradosIA);
        dashboard.put("totalRespostas", totalRespostas);
        dashboard.put("revisoesRealizadas", revisoesRealizadas);
        dashboard.put("revisoesPendentesTotal", revisoesPendentesTotal);
        dashboard.put("mediaGeralAcerto", mediaGeralAcerto);
        dashboard.put("mediaRetencao", mediaRetencao);
        dashboard.put("tempoMedioResposta", tempoMedioResposta);
        dashboard.put("conteudosDominados", conteudosDominados);
        dashboard.put("conteudosCriticos", conteudosCriticos);
        dashboard.put("probabilidadeMediaEsquecimento", probabilidadeMediaEsquecimento);

        // === Evolução Taxa de Acerto (últimos 30 dias) ===
        List<Map<String, Object>> evolucaoTaxaAcerto = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            List<Resposta> respostasDoDia = todasRespostas.stream()
                    .filter(r -> r.getRespondidoEm() != null && r.getRespondidoEm().toLocalDate().equals(dia))
                    .collect(Collectors.toList());
            double taxa = 0.0;
            if (!respostasDoDia.isEmpty()) {
                long acertosDia = respostasDoDia.stream()
                .filter(r -> Boolean.TRUE.equals(r.getCorreta()))
                .count();
                taxa = Math.round((acertosDia * 100.0 / respostasDoDia.size()) * 10.0) / 10.0;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("data", dia.toString());
            entry.put("taxaAcerto", taxa);
            evolucaoTaxaAcerto.add(entry);
        }
        dashboard.put("evolucaoTaxaAcerto", evolucaoTaxaAcerto);

        // === Evolução Retenção (últimos 14 dias) ===
        List<Map<String, Object>> evolucaoRetencao = new ArrayList<>();
        for (int i = 13; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            long revisoesNaoVencidas = todasRevisoes.stream()
                    .filter(r -> r.getProximaRevisao() != null && r.getProximaRevisao().isAfter(dia))
                    .count();
            double retencao = totalRevisoes > 0
                    ? Math.round((revisoesNaoVencidas * 100.0 / totalRevisoes) * 10.0) / 10.0
                    : 0.0;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("data", dia.toString());
            entry.put("retencao", retencao);
            evolucaoRetencao.add(entry);
        }
        dashboard.put("evolucaoRetencao", evolucaoRetencao);

        // === Respostas por Dia (últimos 30 dias) ===
        List<Map<String, Object>> respostasPorDia = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            List<Resposta> respostasDoDia = todasRespostas.stream()
                    .filter(r -> r.getRespondidoEm() != null && r.getRespondidoEm().toLocalDate().equals(dia))
                    .collect(Collectors.toList());
            long totalDia = respostasDoDia.size();
            long acertosDia = respostasDoDia.stream().filter(r -> Boolean.TRUE.equals(r.getCorreta())).count();
            long errosDia = totalDia - acertosDia;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("data", dia.toString());
            entry.put("total", totalDia);
            entry.put("acertos", acertosDia);
            entry.put("erros", errosDia);
            respostasPorDia.add(entry);
        }
        dashboard.put("respostasPorDia", respostasPorDia);

        // === Conteúdos por HSK ===
        Map<Integer, Long> conteudosPorHskMap = todosConteudos.stream()
                .filter(c -> c.getNivelHsk() != null)
                .collect(Collectors.groupingBy(Conteudo::getNivelHsk, Collectors.counting()));
        List<Map<String, Object>> conteudosPorHsk = conteudosPorHskMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("nivel", e.getKey());
                    entry.put("quantidade", e.getValue());
                    return entry;
                })
                .collect(Collectors.toList());
        dashboard.put("conteudosPorHsk", conteudosPorHsk);

        // === Erros por Tema ===
        Map<String, Long> errosPorTema = todasRespostas.stream()
                .filter(r -> Boolean.FALSE.equals(r.getCorreta()))
                .filter(r -> r.getExercicio() != null && r.getExercicio().getConteudo() != null
                        && r.getExercicio().getConteudo().getTemas() != null
                        && !r.getExercicio().getConteudo().getTemas().isEmpty())
                .collect(Collectors.groupingBy(
                        r -> r.getExercicio().getConteudo().getTemas().iterator().next().getNome(),
                        Collectors.counting()
                ));
        dashboard.put("errosPorTema", errosPorTema);

        // === Revisões por Prioridade ===
        long alta = 0, media = 0, baixa = 0;
        for (Revisao revisao : todasRevisoes) {
            int score = calcularScorePrioridadeSimples(revisao, todasRespostas);
            if (score >= 70) alta++;
            else if (score >= 40) media++;
            else baixa++;
        }
        Map<String, Long> revisoesPorPrioridade = new LinkedHashMap<>();
        revisoesPorPrioridade.put("ALTA", alta);
        revisoesPorPrioridade.put("MEDIA", media);
        revisoesPorPrioridade.put("BAIXA", baixa);
        dashboard.put("revisoesPorPrioridade", revisoesPorPrioridade);

        // === Conteúdos Mais Difíceis (top 10 por lapsos) ===
        List<Map<String, Object>> conteudosMaisDificeis = todasRevisoes.stream()
                .filter(r -> r.getConteudo() != null)
                .sorted(Comparator.comparingInt(Revisao::getLapsos).reversed())
                .limit(10)
                .map(r -> {
                    Conteudo c = r.getConteudo();
                    long totalResp = todasRespostas.stream()
                            .filter(resp -> resp.getExercicio() != null
                                    && resp.getExercicio().getConteudo() != null
                                    && resp.getExercicio().getConteudo().getId().equals(c.getId()))
                            .count();
                    long acertosResp = todasRespostas.stream()
                            .filter(resp -> Boolean.TRUE.equals(resp.getCorreta())
                                    && resp.getExercicio() != null
                                    && resp.getExercicio().getConteudo() != null
                                    && resp.getExercicio().getConteudo().getId().equals(c.getId()))
                            .count();
                    double taxaAcerto = totalResp > 0
                            ? Math.round((acertosResp * 100.0 / totalResp) * 10.0) / 10.0
                            : 0.0;
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("hanzi", c.getHanzi());
                    entry.put("pinyin", c.getPinyin());
                    entry.put("traducao", c.getTraducao());
                    entry.put("lapsos", r.getLapsos());
                    entry.put("taxaAcerto", taxaAcerto);
                    return entry;
                })
                .collect(Collectors.toList());
        dashboard.put("conteudosMaisDificeis", conteudosMaisDificeis);

        // === Top Palavras Erradas (top 10) ===
        Map<Conteudo, Long> errosPorConteudo = todasRespostas.stream()
                .filter(r -> Boolean.FALSE.equals(r.getCorreta()))
                .filter(r -> r.getExercicio() != null && r.getExercicio().getConteudo() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getExercicio().getConteudo(),
                        Collectors.counting()
                ));
        List<Map<String, Object>> topPalavrasErradas = errosPorConteudo.entrySet().stream()
                .sorted(Map.Entry.<Conteudo, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    Conteudo c = e.getKey();
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("hanzi", c.getHanzi());
                    entry.put("pinyin", c.getPinyin());
                    entry.put("traducao", c.getTraducao());
                    entry.put("erros", e.getValue());
                    return entry;
                })
                .collect(Collectors.toList());
        dashboard.put("topPalavrasErradas", topPalavrasErradas);

        // === Exercícios por Dia (mesmo que respostasPorDia) ===
        dashboard.put("exerciciosPorDia", respostasPorDia);

        // === Conteúdos Gerados IA por Dia (últimos 30 dias) ===
        List<Map<String, Object>> conteudosGeradosIAPorDia = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            long quantidade = todosConteudosIa.stream()
                    .filter(c -> c.getCriadoEm() != null && c.getCriadoEm().toLocalDate().equals(dia))
                    .count();
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("data", dia.toString());
            entry.put("quantidade", quantidade);
            conteudosGeradosIAPorDia.add(entry);
        }
        dashboard.put("conteudosGeradosIAPorDia", conteudosGeradosIAPorDia);

        // === Tempo Médio por Dia (últimos 30 dias) ===
        List<Map<String, Object>> tempoMedioPorDia = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            OptionalDouble tempoOpt = todasRespostas.stream()
                    .filter(r -> r.getRespondidoEm() != null && r.getRespondidoEm().toLocalDate().equals(dia))
                    .filter(r -> r.getTempoRespostaSegundos() != null).mapToDouble(r -> r.getTempoRespostaSegundos().doubleValue())
                    .average();
            double tempoMedio = tempoOpt.isPresent() ? Math.round(tempoOpt.getAsDouble() * 10.0) / 10.0 : 0.0;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("data", dia.toString());
            entry.put("tempoMedio", tempoMedio);
            tempoMedioPorDia.add(entry);
        }
        dashboard.put("tempoMedioPorDia", tempoMedioPorDia);

        // === Usuários Ativos por Semana (últimas 8 semanas) ===
        List<Map<String, Object>> usuariosAtivosPorSemana = new ArrayList<>();
        for (int i = 7; i >= 0; i--) {
            LocalDate inicioSemana = hoje.minusWeeks(i).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate fimSemana = inicioSemana.plusDays(6);
            long ativos = todosUsuarios.stream()
                    .filter(u -> todasRespostas.stream()
                            .anyMatch(r -> r.getUsuarioId() != null
                                    && r.getUsuarioId().equals(u.getId())
                                    && r.getRespondidoEm() != null
                                    && !r.getRespondidoEm().toLocalDate().isBefore(inicioSemana)
                                    && !r.getRespondidoEm().toLocalDate().isAfter(fimSemana)))
                    .count();
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("semana", inicioSemana.toString());
            entry.put("ativos", ativos);
            usuariosAtivosPorSemana.add(entry);
        }
        dashboard.put("usuariosAtivosPorSemana", usuariosAtivosPorSemana);

        // === Ranking Alunos (top 10 por taxa de acerto) ===
        List<Map<String, Object>> rankingAlunos = todosUsuarios.stream()
                .filter(u -> u.getRole() == null || u.getRole() != com.tcc.mandarim.entity.enums.Role.ADMIN)
                .map(u -> {
                    List<Resposta> respostasUsuario = todasRespostas.stream()
                            .filter(r -> r.getUsuarioId() != null && r.getUsuarioId().equals(u.getId()))
                            .collect(Collectors.toList());
                    long totalResp = respostasUsuario.size();
                    long acertosResp = respostasUsuario.stream().filter(r -> Boolean.TRUE.equals(r.getCorreta())).count();
                    double taxa = totalResp > 0
                            ? Math.round((acertosResp * 100.0 / totalResp) * 10.0) / 10.0
                            : 0.0;
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("id", u.getId());
                    entry.put("nome", u.getNome());
                    entry.put("totalRespostas", totalResp);
                    entry.put("taxaAcerto", taxa);
                    return entry;
                })
                .filter(m -> (long) m.get("totalRespostas") > 0)
                .sorted((a, b) -> Double.compare((double) b.get("taxaAcerto"), (double) a.get("taxaAcerto")))
                .limit(10)
                .collect(Collectors.toList());
        dashboard.put("rankingAlunos", rankingAlunos);

        // === Alunos Baixa Atividade (sem respostas nos últimos 7 dias) ===
        LocalDate seteDiasAtras = hoje.minusDays(7);
        List<Map<String, Object>> alunosBaixaAtividade = todosUsuarios.stream()
                .filter(u -> u.getRole() == null || u.getRole() != com.tcc.mandarim.entity.enums.Role.ADMIN)
                .filter(u -> todasRespostas.stream()
                        .noneMatch(r -> r.getUsuarioId() != null
                                && r.getUsuarioId().equals(u.getId())
                                && r.getRespondidoEm() != null
                                && r.getRespondidoEm().toLocalDate().isAfter(seteDiasAtras)))
                .map(u -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("id", u.getId());
                    entry.put("nome", u.getNome());
                    entry.put("ultimoLogin", u.getUltimoLogin());
                    return entry;
                })
                .collect(Collectors.toList());
        dashboard.put("alunosBaixaAtividade", alunosBaixaAtividade);

        // === Insights ===
        List<String> insights = new ArrayList<>();

        // Tema com maior taxa de erro
        if (!errosPorTema.isEmpty()) {
            Map.Entry<String, Long> piortema = errosPorTema.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);
            if (piortema != null) {
                long totalRespostasTema = todasRespostas.stream()
                        .filter(r -> r.getExercicio() != null && r.getExercicio().getConteudo() != null
                                && r.getExercicio().getConteudo().getTemas() != null
                                && r.getExercicio().getConteudo().getTemas().stream()
                                    .anyMatch(t -> t.getNome().equals(piortema.getKey())))
                        .count();
                double taxaErro = totalRespostasTema > 0
                        ? Math.round((piortema.getValue() * 100.0 / totalRespostasTema) * 10.0) / 10.0
                        : 0.0;
                insights.add("O tema " + piortema.getKey() + " apresenta maior taxa de erro (" + taxaErro + "%).");
            }
        }

        // HSK com menor retenção
        Map<Integer, List<Revisao>> revisoesPorHsk = todasRevisoes.stream()
                .filter(r -> r.getConteudo() != null && r.getConteudo().getNivelHsk() != null)
                .collect(Collectors.groupingBy(r -> r.getConteudo().getNivelHsk()));
        if (!revisoesPorHsk.isEmpty()) {
            int hskMenorRetencao = revisoesPorHsk.entrySet().stream()
                    .min(Comparator.comparingDouble(e -> {
                        long dominados = e.getValue().stream()
                                .filter(r -> r.getRepeticoes() >= 3 && r.getLapsos() <= 1)
                                .count();
                        return e.getValue().isEmpty() ? 0.0 : (dominados * 100.0 / e.getValue().size());
                    }))
                    .map(Map.Entry::getKey)
                    .orElse(1);
            insights.add("O HSK " + hskMenorRetencao + " possui menor retenção.");
        }

        // Revisões última semana vs semana anterior
        long revisoesUltimaSemana = todasRevisoes.stream()
                .filter(r -> r.getUltimaRevisao() != null && r.getUltimaRevisao().toLocalDate().isAfter(hoje.minusDays(7)))
                .count();
        long revisoesSemanaAnterior = todasRevisoes.stream()
                .filter(r -> r.getUltimaRevisao() != null
                        && r.getUltimaRevisao().toLocalDate().isAfter(hoje.minusDays(14))
                        && !r.getUltimaRevisao().toLocalDate().isAfter(hoje.minusDays(7)))
                .count();
        if (revisoesSemanaAnterior > 0) {
            double variacao = Math.round(((revisoesUltimaSemana - revisoesSemanaAnterior) * 100.0 / revisoesSemanaAnterior) * 10.0) / 10.0;
            insights.add("As revisões aumentaram " + variacao + "% na última semana.");
        }

        // Conteúdos gerados pela IA
        insights.add("A IA gerou " + conteudosGeradosIA + " novos conteúdos.");

        // Alunos inativos
        long inativos = alunosBaixaAtividade.size();
        insights.add(inativos + " alunos estão inativos há mais de 7 dias.");

        dashboard.put("insights", insights);

        return dashboard;
    }

    public Map<String, Object> alunoDashboard(UUID usuarioId) {
        Map<String, Object> dashboard = new LinkedHashMap<>();

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Resposta> respostasUsuario = respostaRepository.findByUsuarioId(usuarioId);

        List<Revisao> revisoesUsuario = revisaoRepository.findByUsuarioId(usuarioId);

        LocalDate hoje = LocalDate.now();

        // === Basic ===
        dashboard.put("nome", usuario.getNome());
        dashboard.put("nivelHsk", usuario.getNivelHskAtual());

        // === KPIs ===
        long totalRespostas = respostasUsuario.size();
        long acertos = respostasUsuario.stream().filter(r -> Boolean.TRUE.equals(r.getCorreta())).count();
        double taxaAcerto = totalRespostas > 0
                ? Math.round((acertos * 100.0 / totalRespostas) * 10.0) / 10.0
                : 0.0;

        long conteudosDominados = revisoesUsuario.stream()
                .filter(r -> r.getRepeticoes() >= 3 && r.getLapsos() <= 1)
                .count();
        long totalRevisoes = revisoesUsuario.size();
        double retencao = totalRevisoes > 0
                ? Math.round((conteudosDominados * 100.0 / totalRevisoes) * 10.0) / 10.0
                : 0.0;

        long conteudosEmAprendizado = totalRevisoes - conteudosDominados;

        double tempoMedioResposta = respostasUsuario.stream()
                .filter(r -> r.getTempoRespostaSegundos() != null).mapToDouble(r -> r.getTempoRespostaSegundos().doubleValue())
                .average()
                .orElse(0.0);
        tempoMedioResposta = Math.round(tempoMedioResposta * 10.0) / 10.0;

        int sequenciaDias = calcularSequenciaDias(respostasUsuario);

        long revisoesPendentes = revisoesUsuario.stream()
                .filter(r -> r.getProximaRevisao() != null && !r.getProximaRevisao().isAfter(hoje))
                .count();

        double probabilidadeMediaEsquecimento = revisoesUsuario.stream()
                .mapToDouble(this::calcularProbabilidadeEsquecimento)
                .average()
                .orElse(0.0);
        probabilidadeMediaEsquecimento = Math.round(probabilidadeMediaEsquecimento * 10.0) / 10.0;

        long totalExerciciosRespondidos = totalRespostas;

        long totalConteudosEstudados = respostasUsuario.stream()
                .filter(r -> r.getExercicio() != null && r.getExercicio().getConteudo() != null)
                .map(r -> r.getExercicio().getConteudo().getId())
                .distinct()
                .count();

        dashboard.put("taxaAcerto", taxaAcerto);
        dashboard.put("retencao", retencao);
        dashboard.put("conteudosDominados", conteudosDominados);
        dashboard.put("conteudosEmAprendizado", conteudosEmAprendizado);
        dashboard.put("tempoMedioResposta", tempoMedioResposta);
        dashboard.put("sequenciaDias", sequenciaDias);
        dashboard.put("revisoesPendentes", revisoesPendentes);
        dashboard.put("probabilidadeMediaEsquecimento", probabilidadeMediaEsquecimento);
        dashboard.put("totalExerciciosRespondidos", totalExerciciosRespondidos);
        dashboard.put("totalConteudosEstudados", totalConteudosEstudados);

        // === Charts ===

        // Evolução Taxa de Acerto (últimos 30 dias)
        List<Map<String, Object>> evolucaoTaxaAcerto = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            List<Resposta> respostasDoDia = respostasUsuario.stream()
                    .filter(r -> r.getRespondidoEm() != null && r.getRespondidoEm().toLocalDate().equals(dia))
                    .collect(Collectors.toList());
            double taxa = 0.0;
            if (!respostasDoDia.isEmpty()) {
                long acertosDia = respostasDoDia.stream()
                .filter(r -> Boolean.TRUE.equals(r.getCorreta()))
                .count();
                taxa = Math.round((acertosDia * 100.0 / respostasDoDia.size()) * 10.0) / 10.0;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("data", dia.toString());
            entry.put("taxaAcerto", taxa);
            evolucaoTaxaAcerto.add(entry);
        }
        dashboard.put("evolucaoTaxaAcerto", evolucaoTaxaAcerto);

        // Evolução Retenção (últimos 14 dias)
        List<Map<String, Object>> evolucaoRetencao = new ArrayList<>();
        for (int i = 13; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            long revisoesNaoVencidas = revisoesUsuario.stream()
                    .filter(r -> r.getProximaRevisao() != null && r.getProximaRevisao().isAfter(dia))
                    .count();
            double retencaoDia = totalRevisoes > 0
                    ? Math.round((revisoesNaoVencidas * 100.0 / totalRevisoes) * 10.0) / 10.0
                    : 0.0;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("data", dia.toString());
            entry.put("retencao", retencaoDia);
            evolucaoRetencao.add(entry);
        }
        dashboard.put("evolucaoRetencao", evolucaoRetencao);

        // Tempo Médio por Dia (últimos 30 dias)
        List<Map<String, Object>> tempoMedioPorDia = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            OptionalDouble tempoOpt = respostasUsuario.stream()
                    .filter(r -> r.getRespondidoEm() != null && r.getRespondidoEm().toLocalDate().equals(dia))
                    .filter(r -> r.getTempoRespostaSegundos() != null).mapToDouble(r -> r.getTempoRespostaSegundos().doubleValue())
                    .average();
            double tempoMedio = tempoOpt.isPresent() ? Math.round(tempoOpt.getAsDouble() * 10.0) / 10.0 : 0.0;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("data", dia.toString());
            entry.put("tempoMedio", tempoMedio);
            tempoMedioPorDia.add(entry);
        }
        dashboard.put("tempoMedioPorDia", tempoMedioPorDia);

        // Conteúdos Estudados por Dia (últimos 30 dias)
        List<Map<String, Object>> conteudosEstudadosPorDia = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            long quantidade = respostasUsuario.stream()
                    .filter(r -> r.getRespondidoEm() != null && r.getRespondidoEm().toLocalDate().equals(dia))
                    .filter(r -> r.getExercicio() != null && r.getExercicio().getConteudo() != null)
                    .map(r -> r.getExercicio().getConteudo().getId())
                    .distinct()
                    .count();
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("data", dia.toString());
            entry.put("quantidade", quantidade);
            conteudosEstudadosPorDia.add(entry);
        }
        dashboard.put("conteudosEstudadosPorDia", conteudosEstudadosPorDia);

        // Erros por Tema
        Map<String, Long> errosPorTema = respostasUsuario.stream()
                .filter(r -> Boolean.FALSE.equals(r.getCorreta()))
                .filter(r -> r.getExercicio() != null && r.getExercicio().getConteudo() != null
                        && r.getExercicio().getConteudo().getTemas() != null
                        && !r.getExercicio().getConteudo().getTemas().isEmpty())
                .collect(Collectors.groupingBy(
                        r -> r.getExercicio().getConteudo().getTemas().iterator().next().getNome(),
                        Collectors.counting()
                ));
        dashboard.put("errosPorTema", errosPorTema);

        // Desempenho por HSK
        Map<Integer, List<Resposta>> respostasPorHsk = respostasUsuario.stream()
                .filter(r -> r.getExercicio() != null && r.getExercicio().getConteudo() != null
                        && r.getExercicio().getConteudo().getNivelHsk() != null)
                .collect(Collectors.groupingBy(r -> r.getExercicio().getConteudo().getNivelHsk()));
        List<Map<String, Object>> desempenhoPorHsk = respostasPorHsk.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    long total = e.getValue().size();
                    long acertosHsk = e.getValue().stream().filter(r -> Boolean.TRUE.equals(r.getCorreta())).count();
                    double taxaHsk = total > 0
                            ? Math.round((acertosHsk * 100.0 / total) * 10.0) / 10.0
                            : 0.0;
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("nivel", e.getKey());
                    entry.put("taxaAcerto", taxaHsk);
                    entry.put("total", total);
                    return entry;
                })
                .collect(Collectors.toList());
        dashboard.put("desempenhoPorHsk", desempenhoPorHsk);

        // === Recomendação Inteligente ===
        List<Map<String, Object>> recomendacoes = revisoesUsuario.stream()
                .filter(r -> r.getConteudo() != null)
                .map(r -> {
                    Conteudo c = r.getConteudo();
                    long totalResp = respostasUsuario.stream()
                            .filter(resp -> resp.getExercicio() != null
                                    && resp.getExercicio().getConteudo() != null
                                    && resp.getExercicio().getConteudo().getId().equals(c.getId()))
                            .count();
                    long acertosResp = respostasUsuario.stream()
                            .filter(resp -> Boolean.TRUE.equals(resp.getCorreta())
                                    && resp.getExercicio() != null
                                    && resp.getExercicio().getConteudo() != null
                                    && resp.getExercicio().getConteudo().getId().equals(c.getId()))
                            .count();
                    double taxaAcertoConteudo = totalResp > 0 ? (acertosResp * 100.0 / totalResp) : 100.0;
                    int score = calcularScorePrioridade(r, taxaAcertoConteudo);
                    double probEsquecimento = calcularProbabilidadeEsquecimento(r);
                    long diasSemRevisao = r.getUltimaRevisao() != null
                            ? ChronoUnit.DAYS.between(r.getUltimaRevisao().toLocalDate(), hoje)
                            : 30;
                    String motivo = gerarMotivo(r, taxaAcertoConteudo, diasSemRevisao);
                    String prioridade = score >= 70 ? "ALTA" : (score >= 40 ? "MEDIA" : "BAIXA");

                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("conteudoId", c.getId());
                    entry.put("hanzi", c.getHanzi());
                    entry.put("pinyin", c.getPinyin());
                    entry.put("traducao", c.getTraducao());
                    entry.put("motivo", motivo);
                    entry.put("prioridade", prioridade);
                    entry.put("scorePrioridade", score);
                    entry.put("probabilidadeEsquecimento", Math.round(probEsquecimento * 10.0) / 10.0);
                    return entry;
                })
                .sorted((a, b) -> Integer.compare((int) b.get("scorePrioridade"), (int) a.get("scorePrioridade")))
                .limit(5)
                .collect(Collectors.toList());
        dashboard.put("recomendacoes", recomendacoes);

        // === Plano de Estudos ===
        int exerciciosRecomendados = (int) Math.min(5, revisoesPendentes + 2);
        int revisoesRecomendadas = (int) Math.min(revisoesPendentes, 3);
        int conteudosNovos = 2;
        int tempoEstimadoMinutos = exerciciosRecomendados * 2 + revisoesRecomendadas * 1 + conteudosNovos * 3;

        Map<String, Object> planoEstudos = new LinkedHashMap<>();
        planoEstudos.put("exerciciosRecomendados", exerciciosRecomendados);
        planoEstudos.put("revisoesRecomendadas", revisoesRecomendadas);
        planoEstudos.put("conteudosNovos", conteudosNovos);
        planoEstudos.put("tempoEstimadoMinutos", tempoEstimadoMinutos);
        dashboard.put("planoEstudos", planoEstudos);

        // === Meu Nível ===
        long conteudosCriticos = revisoesUsuario.stream()
                .filter(r -> r.getLapsos() >= 2)
                .count();

        List<Map<String, Object>> evolucaoSemanal = new ArrayList<>();
        for (int i = 7; i >= 0; i--) {
            LocalDate inicioSemana = hoje.minusWeeks(i).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            long dominadosNaSemana = revisoesUsuario.stream()
                    .filter(r -> r.getRepeticoes() >= 3 && r.getLapsos() <= 1)
                    .filter(r -> r.getUltimaRevisao() != null
                            && !r.getUltimaRevisao().toLocalDate().isBefore(inicioSemana)
                            && !r.getUltimaRevisao().toLocalDate().isAfter(inicioSemana.plusDays(6)))
                    .count();
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("semana", inicioSemana.toString());
            entry.put("dominados", dominadosNaSemana);
            evolucaoSemanal.add(entry);
        }

        Map<String, Object> meuNivel = new LinkedHashMap<>();
        meuNivel.put("conteudosDominados", conteudosDominados);
        meuNivel.put("conteudosCriticos", conteudosCriticos);
        meuNivel.put("retencao", retencao);
        meuNivel.put("evolucaoSemanal", evolucaoSemanal);
        dashboard.put("meuNivel", meuNivel);

        // === Palavras Difíceis (top 5 por lapsos) ===
        List<Map<String, Object>> palavrasDificeis = revisoesUsuario.stream()
                .filter(r -> r.getConteudo() != null)
                .sorted(Comparator.comparingInt(Revisao::getLapsos).reversed())
                .limit(5)
                .map(r -> {
                    Conteudo c = r.getConteudo();
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("hanzi", c.getHanzi());
                    entry.put("pinyin", c.getPinyin());
                    entry.put("traducao", c.getTraducao());
                    entry.put("lapsos", r.getLapsos());
                    entry.put("repeticoes", r.getRepeticoes());
                    return entry;
                })
                .collect(Collectors.toList());
        dashboard.put("palavrasDificeis", palavrasDificeis);

        // === Próxima Revisão ===
        revisoesUsuario.stream()
                .filter(r -> r.getProximaRevisao() != null && !r.getProximaRevisao().isAfter(hoje))
                .filter(r -> r.getConteudo() != null)
                .min(Comparator.comparing(Revisao::getProximaRevisao))
                .ifPresentOrElse(r -> {
                    Conteudo c = r.getConteudo();
                    Map<String, Object> proximaRevisao = new LinkedHashMap<>();
                    proximaRevisao.put("revisaoId", r.getId());
                    proximaRevisao.put("conteudoId", c.getId());
                    proximaRevisao.put("hanzi", c.getHanzi());
                    proximaRevisao.put("pinyin", c.getPinyin());
                    proximaRevisao.put("traducao", c.getTraducao());
                    dashboard.put("proximaRevisao", proximaRevisao);
                }, () -> dashboard.put("proximaRevisao", null));

        // === Evolução 30 Dias (legacy compatibility) ===
        List<Map<String, Object>> evolucao30Dias = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            List<Resposta> respostasDoDia = respostasUsuario.stream()
                    .filter(r -> r.getRespondidoEm() != null && r.getRespondidoEm().toLocalDate().equals(dia))
                    .collect(Collectors.toList());
            long totalDia = respostasDoDia.size();
            long acertosDia = respostasDoDia.stream().filter(r -> Boolean.TRUE.equals(r.getCorreta())).count();
            double taxaDia = totalDia > 0
                    ? Math.round((acertosDia * 100.0 / totalDia) * 10.0) / 10.0
                    : 0.0;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("data", dia.toString());
            entry.put("total", totalDia);
            entry.put("acertos", acertosDia);
            entry.put("taxaAcerto", taxaDia);
            evolucao30Dias.add(entry);
        }
        dashboard.put("evolucao30Dias", evolucao30Dias);

        return dashboard;
    }

    // === Helper Methods ===

    private int calcularSequenciaDias(List<Resposta> respostas) {
        LocalDate hoje = LocalDate.now();
        Set<LocalDate> diasComResposta = respostas.stream()
                .filter(r -> r.getRespondidoEm() != null)
                .map(r -> r.getRespondidoEm().toLocalDate())
                .collect(Collectors.toSet());

        int sequencia = 0;
        LocalDate dia = hoje;
        while (diasComResposta.contains(dia)) {
            sequencia++;
            dia = dia.minusDays(1);
        }
        return sequencia;
    }

    private List<Map<String, Object>> calcularEvolucao(List<Resposta> respostas, int dias) {
        LocalDate hoje = LocalDate.now();
        List<Map<String, Object>> evolucao = new ArrayList<>();
        for (int i = dias - 1; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            List<Resposta> respostasDoDia = respostas.stream()
                    .filter(r -> r.getRespondidoEm() != null && r.getRespondidoEm().toLocalDate().equals(dia))
                    .collect(Collectors.toList());
            long total = respostasDoDia.size();
            long acertosDia = respostasDoDia.stream().filter(r -> Boolean.TRUE.equals(r.getCorreta())).count();
            double taxa = total > 0 ? Math.round((acertosDia * 100.0 / total) * 10.0) / 10.0 : 0.0;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("data", dia.toString());
            entry.put("total", total);
            entry.put("acertos", acertosDia);
            entry.put("taxaAcerto", taxa);
            evolucao.add(entry);
        }
        return evolucao;
    }

    private double calcularProbabilidadeEsquecimento(Revisao revisao) {
        LocalDate hoje = LocalDate.now();
        long diasDesdeUltimaRevisao = revisao.getUltimaRevisao() != null
                ? ChronoUnit.DAYS.between(revisao.getUltimaRevisao().toLocalDate(), LocalDate.now())
                : 30;
        double intervaloDias = revisao.getIntervaloDias() > 0 ? revisao.getIntervaloDias() : 1;
        double probabilidade = (diasDesdeUltimaRevisao / intervaloDias) * 50 + revisao.getLapsos() * 10;
        return Math.min(100, probabilidade);
    }

    private int calcularScorePrioridade(Revisao revisao, double taxaAcertoConteudo) {
        LocalDate hoje = LocalDate.now();
        int score = 0;

        // +40 se está vencida
        if (revisao.getProximaRevisao() != null && !revisao.getProximaRevisao().isAfter(hoje)) {
            score += 40;
        }

        // +25 se taxa de acerto < 70%
        if (taxaAcertoConteudo < 70) {
            score += 25;
        }

        // +20 se lapsos >= 2
        if (revisao.getLapsos() >= 2) {
            score += 20;
        }

        return score;
    }

    private int calcularScorePrioridadeSimples(Revisao revisao, List<Resposta> todasRespostas) {
        LocalDate hoje = LocalDate.now();
        int score = 0;

        if (revisao.getProximaRevisao() != null && !revisao.getProximaRevisao().isAfter(hoje)) {
            score += 40;
        }

        if (revisao.getConteudo() != null) {
            Long conteudoId = revisao.getConteudo().getId();
            long totalResp = todasRespostas.stream()
                    .filter(r -> r.getExercicio() != null
                            && r.getExercicio().getConteudo() != null
                            && r.getExercicio().getConteudo().getId().equals(conteudoId))
                    .count();
            long acertosResp = todasRespostas.stream()
                    .filter(r -> Boolean.TRUE.equals(r.getCorreta())
                            && r.getExercicio() != null
                            && r.getExercicio().getConteudo() != null
                            && r.getExercicio().getConteudo().getId().equals(conteudoId))
                    .count();
            double taxaAcerto = totalResp > 0 ? (acertosResp * 100.0 / totalResp) : 100.0;
            if (taxaAcerto < 70) {
                score += 25;
            }
        }

        if (revisao.getLapsos() >= 2) {
            score += 20;
        }

        return score;
    }

    private String gerarMotivo(Revisao revisao, double taxaAcerto, long diasSemRevisao) {
        List<String> motivos = new ArrayList<>();

        if (revisao.getLapsos() >= 2) {
            motivos.add("Você errou esta palavra " + revisao.getLapsos() + " vezes.");
        }

        if (diasSemRevisao >= 3) {
            motivos.add("Está há " + diasSemRevisao + " dias sem revisar.");
        }

        if (taxaAcerto < 70) {
            motivos.add("Taxa de acerto baixa (" + Math.round(taxaAcerto * 10.0) / 10.0 + "%).");
        }

        List<Resposta> respostasConteudo = respostaRepository.findAll().stream()
                .filter(r -> r.getExercicio() != null
                        && r.getExercicio().getConteudo() != null
                        && revisao.getConteudo() != null
                        && r.getExercicio().getConteudo().getId().equals(revisao.getConteudo().getId()))
                .collect(Collectors.toList());
        if (!respostasConteudo.isEmpty()) {
            double tempoMedio = respostasConteudo.stream()
                    .filter(r -> r.getTempoRespostaSegundos() != null).mapToDouble(r -> r.getTempoRespostaSegundos().doubleValue())
                    .average()
                    .orElse(0.0);
            double tempoMedioGeral = respostaRepository.findAll().stream()
                    .filter(r -> r.getTempoRespostaSegundos() != null).mapToDouble(r -> r.getTempoRespostaSegundos().doubleValue())
                    .average()
                    .orElse(0.0);
            if (tempoMedio > tempoMedioGeral * 1.5) {
                motivos.add("Tempo médio acima da média.");
            }
        }

        return motivos.isEmpty() ? "Revisão recomendada." : String.join(" ", motivos);
    }
}
