package com.tcc.mandarim.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConteudoIaResponse {

    private String id;
    private String prompt;
    private ParametrosIA parametros;
    private ConteudoGeradoIA conteudoGerado;
    private List<ConteudoGeradoIA> conteudosGerados;
    private String status;
    private Long tempoProcessamentoMs;
    private String dataCriacao;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParametrosIA {
        private String tipo;
        private String tema;
        private Integer nivelHsk;
        private Integer quantidade;
        private String objetivo;
        private String idiomaTraduzao;
        private String usuarioId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConteudoGeradoIA {
        private String hanzi;
        private String pinyin;
        private String traducao;
        private String explicacao;
        private String exemploHanzi;
        private String exemploPinyin;
        private String exemploTraduzido;
        private Integer dificuldade;
        private String categoria;
        private List<String> tags;
    }
}
