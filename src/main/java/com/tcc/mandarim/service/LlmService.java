package com.tcc.mandarim.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcc.mandarim.dto.request.GerarConteudoIaRequest;
import com.tcc.mandarim.dto.response.ConteudoIaResponse.ConteudoGeradoIA;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serviço responsável pela comunicação com o LLM (OpenAI) via Spring AI.
 * Monta prompts especializados para geração de conteúdo de mandarim.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    /**
     * Gera conteúdos de mandarim com base nos parâmetros estruturados.
     * Retorna a lista de conteúdos gerados e o prompt utilizado.
     */
    public LlmResult gerarConteudo(GerarConteudoIaRequest request, List<String> palavrasExistentes) {
        String prompt = montarPromptConteudo(request, palavrasExistentes);
        log.info("[LLM] Gerando conteúdo com prompt de {} chars", prompt.length());

        String resposta = chatClientBuilder.build()
                .prompt()
                .user(prompt)
                .call()
                .content();

        log.info("[LLM] Resposta bruta do LLM: {}", resposta);
        List<ConteudoGeradoIA> conteudos = parsearRespostaConteudo(resposta);

        // Log para verificar se hanzi foi parseado
        for (int i = 0; i < conteudos.size(); i++) {
            ConteudoGeradoIA c = conteudos.get(i);
            log.info("[LLM] Item[{}] parseado: hanzi='{}', pinyin='{}', traducao='{}'",
                    i, c.getHanzi(), c.getPinyin(), c.getTraducao());
        }

        return new LlmResult(prompt, conteudos, resposta);
    }

    /**
     * Gera exemplos adicionais para um conteúdo existente.
     */
    public LlmResult gerarExemplos(String hanzi, String pinyin, int nivelHsk, int quantidade) {
        String prompt = montarPromptExemplos(hanzi, pinyin, nivelHsk, quantidade);
        log.info("[LLM] Gerando {} exemplos adicionais para '{}'", quantidade, hanzi);

        String resposta = chatClientBuilder.build()
                .prompt()
                .user(prompt)
                .call()
                .content();

        List<ConteudoGeradoIA> conteudos = parsearRespostaConteudo(resposta);
        return new LlmResult(prompt, conteudos, resposta);
    }

    /**
     * Gera um plano de estudo personalizado baseado no contexto do usuário.
     */
    public String gerarPlanoEstudo(String nomeUsuario, int totalConteudos, int totalAcertos,
                                   int totalErros, int revisoesPendentes) {
        String prompt = montarPromptPlanoEstudo(nomeUsuario, totalConteudos, totalAcertos,
                totalErros, revisoesPendentes);
        log.info("[LLM] Gerando plano de estudo para '{}'", nomeUsuario);

        return chatClientBuilder.build()
                .prompt()
                .user(prompt)
                .call()
                .content();
    }

    // ─── Montagem de prompts ─────────────────────────────────────────────────

    private String montarPromptConteudo(GerarConteudoIaRequest request, List<String> palavrasExistentes) {
        StringBuilder sb = new StringBuilder();
        sb.append("Você é um professor especialista em mandarim chinês. ");
        sb.append("Gere exatamente ").append(request.getQuantidade());
        sb.append(" item(ns) de conteúdo do tipo ").append(request.getTipo());
        sb.append(" para nível HSK ").append(request.getNivelHsk()).append(".\n\n");

        sb.append("Objetivo do aluno: ").append(request.getObjetivo()).append("\n");
        sb.append("Idioma de tradução: ").append(request.getIdiomaTraduzao()).append("\n");

        if (request.getTema() != null && !request.getTema().isBlank()) {
            sb.append("Tema/contexto: ").append(request.getTema()).append("\n");
        }

        // Lista de palavras que já existem no banco — NÃO repetir
        if (palavrasExistentes != null && !palavrasExistentes.isEmpty()) {
            sb.append("\nIMPORTANTE: NÃO gere nenhuma das seguintes palavras/frases que já existem no sistema:\n");
            sb.append(String.join(", ", palavrasExistentes));
            sb.append("\nGere apenas palavras DIFERENTES das listadas acima.\n");
        }

        sb.append("\nResponda APENAS com um array JSON válido (sem markdown, sem ```json). ");
        sb.append("Cada objeto no array deve ter exatamente estes campos:\n");
        sb.append("{\n");
        sb.append("  \"hanzi\": \"caractere(s) chinês(es)\",\n");
        sb.append("  \"pinyin\": \"pronúncia em pinyin com tons\",\n");
        sb.append("  \"traducao\": \"tradução no idioma solicitado\",\n");
        sb.append("  \"explicacao\": \"breve explicação de uso ou contexto\",\n");
        sb.append("  \"exemploHanzi\": \"frase exemplo em caracteres chineses\",\n");
        sb.append("  \"exemploPinyin\": \"pinyin da frase exemplo\",\n");
        sb.append("  \"exemploTraduzido\": \"tradução da frase exemplo\",\n");
        sb.append("  \"dificuldade\": 1-5 (número inteiro),\n");
        sb.append("  \"categoria\": \"categoria gramatical ou temática\",\n");
        sb.append("  \"tags\": [\"tag1\", \"tag2\"]\n");
        sb.append("}\n\n");
        sb.append("Regras:\n");
        sb.append("- O campo \"hanzi\" é OBRIGATÓRIO e deve conter os caracteres chineses (ex: 你好, 吃饭, 公共汽车)\n");
        sb.append("- NUNCA deixe o campo \"hanzi\" vazio ou null\n");
        sb.append("- Use pinyin com marcas de tom (ā, á, ǎ, à, etc.)\n");
        sb.append("- A dificuldade deve ser coerente com o nível HSK solicitado\n");
        sb.append("- Inclua sempre um exemplo de uso em frase completa\n");
        sb.append("- Se tipo=DIALOGO, o hanzi deve conter um mini-diálogo (A: ... B: ...)\n");
        sb.append("- Tags relevantes ao conteúdo (máximo 4)\n");

        return sb.toString();
    }

    private String montarPromptExemplos(String hanzi, String pinyin, int nivelHsk, int quantidade) {
        StringBuilder sb = new StringBuilder();
        sb.append("Você é um professor de mandarim chinês. ");
        sb.append("Para o conteúdo '").append(hanzi).append("' (").append(pinyin).append("), ");
        sb.append("gere ").append(quantidade).append(" exemplos adicionais de uso em frases ");
        sb.append("adequados para nível HSK ").append(nivelHsk).append(".\n\n");
        sb.append("Responda APENAS com um array JSON válido (sem markdown). ");
        sb.append("Cada objeto:\n");
        sb.append("{\n");
        sb.append("  \"hanzi\": \"frase exemplo em chinês\",\n");
        sb.append("  \"pinyin\": \"pinyin da frase\",\n");
        sb.append("  \"traducao\": \"tradução em português\",\n");
        sb.append("  \"explicacao\": \"nota sobre uso ou gramática\",\n");
        sb.append("  \"exemploHanzi\": null,\n");
        sb.append("  \"exemploPinyin\": null,\n");
        sb.append("  \"exemploTraduzido\": null,\n");
        sb.append("  \"dificuldade\": 1-5,\n");
        sb.append("  \"categoria\": \"exemplo de uso\",\n");
        sb.append("  \"tags\": [\"exemplo\"]\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String montarPromptPlanoEstudo(String nome, int totalConteudos, int acertos,
                                           int erros, int revisoes) {
        StringBuilder sb = new StringBuilder();
        sb.append("Você é um tutor de mandarim chinês. Crie um plano de estudo personalizado ");
        sb.append("para o aluno com base nas seguintes estatísticas:\n\n");
        sb.append("- Nome: ").append(nome).append("\n");
        sb.append("- Total de conteúdos estudados: ").append(totalConteudos).append("\n");
        sb.append("- Acertos em exercícios: ").append(acertos).append("\n");
        sb.append("- Erros em exercícios: ").append(erros).append("\n");
        sb.append("- Revisões pendentes: ").append(revisoes).append("\n\n");
        sb.append("Crie um plano de estudo semanal com:\n");
        sb.append("1. Diagnóstico do nível atual\n");
        sb.append("2. Pontos fortes e fracos identificados\n");
        sb.append("3. Recomendações diárias (segunda a domingo)\n");
        sb.append("4. Metas semanais concretas\n");
        sb.append("5. Dicas de estudo personalizadas\n\n");
        sb.append("Responda em português de forma clara e organizada (pode usar formatação com ");
        sb.append("títulos e listas). NÃO use JSON, responda em texto puro formatado.");

        return sb.toString();
    }

    // ─── Parsing ─────────────────────────────────────────────────────────────

    private List<ConteudoGeradoIA> parsearRespostaConteudo(String resposta) {
        try {
            // Remove possíveis blocos de markdown caso o LLM insista
            String json = resposta.trim();
            if (json.startsWith("```")) {
                json = json.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
            }
            return objectMapper.readValue(json, new TypeReference<List<ConteudoGeradoIA>>() {});
        } catch (JsonProcessingException e) {
            log.error("[LLM] Erro ao parsear resposta do LLM: {}", e.getMessage());
            log.debug("[LLM] Resposta bruta: {}", resposta);
            throw new RuntimeException("Erro ao processar resposta da IA. Tente novamente.", e);
        }
    }

    // ─── Result wrapper ──────────────────────────────────────────────────────

    public record LlmResult(String prompt, List<ConteudoGeradoIA> conteudos, String respostaBruta) {}
}
