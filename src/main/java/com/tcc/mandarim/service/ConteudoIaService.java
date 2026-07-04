package com.tcc.mandarim.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcc.mandarim.dto.request.GerarConteudoIaRequest;
import com.tcc.mandarim.dto.request.GerarExemplosRequest;
import com.tcc.mandarim.dto.request.GerarPlanoEstudoRequest;
import com.tcc.mandarim.dto.response.ConteudoIaResponse;
import com.tcc.mandarim.dto.response.ConteudoIaResponse.ConteudoGeradoIA;
import com.tcc.mandarim.dto.response.ConteudoIaResponse.ParametrosIA;
import com.tcc.mandarim.dto.response.PlanoEstudoResponse;
import com.tcc.mandarim.entity.*;
import com.tcc.mandarim.entity.enums.OrigemConteudo;
import com.tcc.mandarim.entity.enums.StatusIA;
import com.tcc.mandarim.entity.enums.TipoConteudo;
import com.tcc.mandarim.entity.enums.TipoExercicio;
import com.tcc.mandarim.exception.ResourceNotFoundException;
import com.tcc.mandarim.repository.*;
import com.tcc.mandarim.service.LlmService.LlmResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConteudoIaService {

    private final ConteudoIaRepository conteudoIaRepository;
    private final ConteudoRepository conteudoRepository;
    private final ExercicioRepository exercicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final RespostaRepository respostaRepository;
    private final RevisaoRepository revisaoRepository;
    private final LlmService llmService;
    private final ObjectMapper objectMapper;

    // ─── Gerar conteúdo estruturado via LLM ──────────────────────────────────

    @Transactional
    public ConteudoIaResponse gerarConteudo(GerarConteudoIaRequest request) {
        long inicio = System.currentTimeMillis();

        // Busca palavras já existentes no banco para evitar duplicatas
        List<String> palavrasExistentes = conteudoRepository.findAll().stream()
                .map(c -> c.getHanzi())
                .filter(h -> h != null && !h.isBlank())
                .collect(Collectors.toList());

        // Chama o LLM com a lista de palavras existentes
        LlmResult resultado = llmService.gerarConteudo(request, palavrasExistentes);

        long tempoMs = System.currentTimeMillis() - inicio;

        // Serializa parâmetros e conteúdos gerados
        String parametrosJson = serializarJson(request);
        String conteudosJson = serializarJson(resultado.conteudos());

        // Persiste
        UUID usuarioId = request.getUsuarioId() != null && !request.getUsuarioId().isBlank()
                ? UUID.fromString(request.getUsuarioId()) : null;

        ConteudoIa entity = ConteudoIa.builder()
                .usuarioId(usuarioId)
                .prompt(resultado.prompt())
                .parametrosJson(parametrosJson)
                .conteudosGeradosJson(conteudosJson)
                .status(StatusIA.PENDENTE)
                .tempoProcessamentoMs(tempoMs)
                .build();

        entity = conteudoIaRepository.save(entity);
        log.info("[IA] Conteúdo gerado em {}ms — id={}", tempoMs, entity.getId());

        return toResponse(entity);
    }

    // ─── Listar pendentes ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ConteudoIaResponse> buscarPendentes() {
        return conteudoIaRepository.findByStatus(StatusIA.PENDENTE).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Aprovar conteúdo (salva em conteúdos + gera exercícios) ─────────────

    @Transactional
    public ConteudoIaResponse aprovar(Long id) {
        ConteudoIa entity = conteudoIaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conteúdo IA", id));

        entity.setStatus(StatusIA.APROVADO);
        entity = conteudoIaRepository.save(entity);

        // Cria conteúdos e exercícios a partir dos dados gerados
        List<ConteudoGeradoIA> conteudos = desserializarConteudos(entity.getConteudosGeradosJson());
        ParametrosIA parametros = desserializarParametros(entity.getParametrosJson());

        for (ConteudoGeradoIA item : conteudos) {
            log.info("[IA] Aprovando item: hanzi='{}', pinyin='{}', traducao='{}'",
                    item.getHanzi(), item.getPinyin(), item.getTraducao());
            criarConteudosEExercicios(item, parametros);
        }

        log.info("[IA] Conteúdo id={} aprovado. {} itens salvos com exercícios.", id, conteudos.size());
        return toResponse(entity);
    }

    // ─── Rejeitar / excluir ──────────────────────────────────────────────────

    @Transactional
    public void rejeitar(Long id) {
        ConteudoIa entity = conteudoIaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conteúdo IA", id));

        entity.setStatus(StatusIA.REJEITADO);
        conteudoIaRepository.save(entity);
        log.info("[IA] Conteúdo id={} rejeitado.", id);
    }

    // ─── Gerar exemplos adicionais ───────────────────────────────────────────

    @Transactional
    public ConteudoIaResponse gerarExemplos(GerarExemplosRequest request) {
        ConteudoIa entity = conteudoIaRepository.findById(request.getConteudoId())
                .orElseThrow(() -> new ResourceNotFoundException("Conteúdo IA", request.getConteudoId()));

        // Pega o primeiro conteúdo gerado para usar como referência
        List<ConteudoGeradoIA> existentes = desserializarConteudos(entity.getConteudosGeradosJson());
        if (existentes.isEmpty()) {
            throw new RuntimeException("Nenhum conteúdo gerado encontrado para gerar exemplos.");
        }

        ConteudoGeradoIA referencia = existentes.get(0);

        // Gera exemplos via LLM
        LlmResult resultado = llmService.gerarExemplos(
                referencia.getHanzi(), referencia.getPinyin(),
                request.getNivelHsk(), request.getQuantidade()
        );

        // Adiciona os novos exemplos à lista existente
        existentes.addAll(resultado.conteudos());
        entity.setConteudosGeradosJson(serializarJson(existentes));
        entity = conteudoIaRepository.save(entity);

        log.info("[IA] {} exemplos adicionais gerados para id={}", resultado.conteudos().size(), entity.getId());
        return toResponse(entity);
    }

    // ─── Gerar plano de estudo ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PlanoEstudoResponse gerarPlanoEstudo(GerarPlanoEstudoRequest request) {
        UUID usuarioId = UUID.fromString(request.getUsuarioId());

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));

        // Coleta estatísticas
        long totalConteudos = conteudoRepository.count();
        long acertos = respostaRepository.countAcertosByUsuarioId(usuarioId);
        long erros = respostaRepository.countErrosByUsuarioId(usuarioId);
        int revisoesPendentes = revisaoRepository
                .findPendentesByUsuarioId(usuarioId, LocalDate.now()).size();

        // Gera plano via LLM
        String plano = llmService.gerarPlanoEstudo(
                usuario.getNome(),
                (int) totalConteudos,
                (int) acertos,
                (int) erros,
                revisoesPendentes
        );

        return PlanoEstudoResponse.builder()
                .id(UUID.randomUUID().toString())
                .usuarioId(request.getUsuarioId())
                .plano(plano)
                .dataCriacao(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    // ─── Helpers privados ────────────────────────────────────────────────────

    private void criarConteudosEExercicios(ConteudoGeradoIA item, ParametrosIA parametros) {
        TipoConteudo tipoSolicitado = TipoConteudo.PALAVRA;
        if (parametros != null && parametros.getTipo() != null) {
            try {
                tipoSolicitado = TipoConteudo.valueOf(parametros.getTipo());
            } catch (IllegalArgumentException e) {
                log.warn("[IA] Tipo inválido '{}', usando PALAVRA", parametros.getTipo());
            }
        }

        Integer nivelHsk = parametros != null ? parametros.getNivelHsk() : 1;
        short dificuldade = item.getDificuldade() != null ? item.getDificuldade().shortValue() : 1;

        // Sempre salva a palavra/vocábulo principal
        log.info("[IA] Salvando palavra — hanzi='{}', pinyin='{}', traducao='{}', explicacao='{}'",
                item.getHanzi(), item.getPinyin(), item.getTraducao(), item.getExplicacao());
        Conteudo palavra = Conteudo.builder()
                .tipo(TipoConteudo.PALAVRA)
                .hanzi(item.getHanzi())
                .pinyin(item.getPinyin())
                .traducao(item.getTraducao())
                .explicacao(item.getExplicacao())
                .nivelHsk(nivelHsk)
                .dificuldade(dificuldade)
                .origem(OrigemConteudo.IA)
                .build();
        palavra = conteudoRepository.save(palavra);
        log.info("[IA] Palavra salva id={}, hanzi no banco='{}'", palavra.getId(), palavra.getHanzi());
        gerarExerciciosParaConteudo(palavra, item);

        // Se tipo solicitado é FRASE ou DIALOGO, salva também a frase de exemplo como conteúdo separado
        if ((tipoSolicitado == TipoConteudo.FRASE || tipoSolicitado == TipoConteudo.DIALOGO)
                && item.getExemploHanzi() != null && !item.getExemploHanzi().isBlank()) {

            Conteudo frase = Conteudo.builder()
                    .tipo(tipoSolicitado)
                    .hanzi(item.getExemploHanzi())
                    .pinyin(item.getExemploPinyin())
                    .traducao(item.getExemploTraduzido())
                    .explicacao(item.getExplicacao())
                    .nivelHsk(nivelHsk)
                    .dificuldade(dificuldade)
                    .origem(OrigemConteudo.IA)
                    .build();
            frase = conteudoRepository.save(frase);

            // Gera exercícios para a frase também
            ConteudoGeradoIA itemFrase = ConteudoGeradoIA.builder()
                    .hanzi(item.getExemploHanzi())
                    .pinyin(item.getExemploPinyin())
                    .traducao(item.getExemploTraduzido())
                    .explicacao(item.getExplicacao())
                    .dificuldade(item.getDificuldade())
                    .build();
            gerarExerciciosParaConteudo(frase, itemFrase);

            log.info("[IA] Frase salva: '{}'", item.getExemploHanzi());
        }
    }

    private void gerarExerciciosParaConteudo(Conteudo conteudo, ConteudoGeradoIA item) {
        // Usa dados do conteúdo salvo no banco (já validado)
        String pinyin = valorOuFallback(conteudo.getPinyin(), item.getPinyin());
        String traducao = valorOuFallback(conteudo.getTraducao(), item.getTraducao());
        short dificuldade = item.getDificuldade() != null ? item.getDificuldade().shortValue() : 1;

        log.info("[IA] Gerando exercícios — pinyin='{}', traducao='{}'", pinyin, traducao);

        // Não gera exercícios se os dados essenciais estão vazios
        if (pinyin.equals("???") || traducao.equals("???")) {
            log.warn("[IA] Dados insuficientes para gerar exercícios: pinyin='{}', traducao='{}'", pinyin, traducao);
            return;
        }

        // 1. Exercício de múltipla escolha — "Qual a tradução de <pinyin>?"
        //    Entre aspas vai o pinyin, resposta esperada é a tradução
        Exercicio multiplaEscolha = Exercicio.builder()
                .conteudo(conteudo)
                .tipo(TipoExercicio.MULTIPLA_ESCOLHA)
                .enunciado("Qual a tradução de \"" + pinyin + "\"?")
                .respostaEsperada(traducao)
                .dificuldade(dificuldade)
                .build();

        // Alternativas: busca traduções de outros conteúdos como distratores
        List<String> distratores = buscarDistratores(conteudo.getId(), traducao, 3);

        List<Alternativa> alternativas = new ArrayList<>();
        alternativas.add(Alternativa.builder()
                .exercicio(multiplaEscolha)
                .texto(traducao)
                .correta(true)
                .build());
        for (String distrator : distratores) {
            alternativas.add(Alternativa.builder()
                    .exercicio(multiplaEscolha)
                    .texto(distrator)
                    .correta(false)
                    .build());
        }
        // Embaralha para que a correta não fique sempre na primeira posição
        Collections.shuffle(alternativas);
        multiplaEscolha.setAlternativas(alternativas);
        exercicioRepository.save(multiplaEscolha);

        // 2. Exercício de pinyin — "Qual o pinyin de <tradução>?"
        //    Entre aspas vai a tradução, resposta esperada é o pinyin
        Exercicio pinyin_ex = Exercicio.builder()
                .conteudo(conteudo)
                .tipo(TipoExercicio.PINYIN)
                .enunciado("Qual o pinyin de \"" + traducao + "\"?")
                .respostaEsperada(pinyin)
                .dificuldade(dificuldade)
                .build();
        exercicioRepository.save(pinyin_ex);

        // 3. Exercício de tradução — "Qual a tradução de <pinyin>?"
        //    Entre aspas vai o pinyin, resposta esperada é a tradução
        Exercicio traducao_ex = Exercicio.builder()
                .conteudo(conteudo)
                .tipo(TipoExercicio.TRADUCAO_MANDARIM_PT)
                .enunciado("Qual a tradução de \"" + pinyin + "\"?")
                .respostaEsperada(traducao)
                .dificuldade(dificuldade)
                .build();
        exercicioRepository.save(traducao_ex);
    }

    private String valorOuFallback(String valor, String fallback) {
        if (valor != null && !valor.isBlank()) return valor.trim();
        if (fallback != null && !fallback.isBlank()) return fallback.trim();
        return "???";
    }

    /**
     * Busca traduções de outros conteúdos para usar como distratores em múltipla escolha.
     * Prioriza conteúdos do mesmo nível HSK para que as opções sejam semelhantes.
     */
    private List<String> buscarDistratores(Long conteudoIdExcluir, String respostaCorreta, int quantidade) {
        List<Conteudo> todosConteudos = conteudoRepository.findAll();

        List<String> candidatos = todosConteudos.stream()
                .filter(c -> !c.getId().equals(conteudoIdExcluir))
                .map(Conteudo::getTraducao)
                .filter(t -> t != null && !t.isBlank() && !t.equalsIgnoreCase(respostaCorreta))
                .distinct()
                .collect(Collectors.toList());

        // Embaralha para pegar aleatoriamente
        Collections.shuffle(candidatos);

        // Pega até a quantidade desejada
        List<String> distratores = candidatos.stream()
                .limit(quantidade)
                .collect(Collectors.toList());

        // Se não tiver conteúdos suficientes no banco, completa com fallbacks genéricos
        List<String> fallbacks = List.of("água", "casa", "pessoa", "tempo", "comida", "livro", "escola", "amigo");
        int idx = 0;
        while (distratores.size() < quantidade && idx < fallbacks.size()) {
            String fb = fallbacks.get(idx);
            if (!fb.equalsIgnoreCase(respostaCorreta) && !distratores.contains(fb)) {
                distratores.add(fb);
            }
            idx++;
        }

        return distratores;
    }

    // ─── Conversão entity → response ─────────────────────────────────────────

    private ConteudoIaResponse toResponse(ConteudoIa entity) {
        List<ConteudoGeradoIA> conteudos = desserializarConteudos(entity.getConteudosGeradosJson());
        ParametrosIA parametros = desserializarParametros(entity.getParametrosJson());

        return ConteudoIaResponse.builder()
                .id(String.valueOf(entity.getId()))
                .prompt(entity.getPrompt())
                .parametros(parametros)
                .conteudosGerados(conteudos)
                .conteudoGerado(conteudos.isEmpty() ? null : conteudos.get(0))
                .status(entity.getStatus().name())
                .tempoProcessamentoMs(entity.getTempoProcessamentoMs())
                .dataCriacao(entity.getCriadoEm() != null
                        ? entity.getCriadoEm().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .build();
    }

    // ─── Serialização / Desserialização JSON ─────────────────────────────────

    private String serializarJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("[IA] Erro ao serializar JSON: {}", e.getMessage());
            return "{}";
        }
    }

    private List<ConteudoGeradoIA> desserializarConteudos(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<ConteudoGeradoIA>>() {});
        } catch (JsonProcessingException e) {
            log.error("[IA] Erro ao desserializar conteúdos: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private ParametrosIA desserializarParametros(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, ParametrosIA.class);
        } catch (JsonProcessingException e) {
            log.error("[IA] Erro ao desserializar parâmetros: {}", e.getMessage());
            return null;
        }
    }
}
