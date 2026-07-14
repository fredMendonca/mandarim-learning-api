package com.tcc.mandarim.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicadoresRevisaoResponse {

    private Integer revisoesPendentes;
    private Integer conteudosCriticos;
    private Double taxaRetencao;
    private Double taxaAcertoRevisoes;
    private Double tempoMedioResposta;
    private Integer conteudosDominados;
    private Integer conteudosEmAprendizado;

    // Dados para gráficos — formato compatível com Recharts
    private List<EvolucaoDesempenho> evolucaoDesempenho;
    private List<EvolucaoRetencao> evolucaoRetencao;
    private List<ErroPorTema> errosPorTema;
    private Map<String, Integer> revisoesPorPrioridade;
    private List<ProbabilidadeEsquecimento> probabilidadeEsquecimento;
    private AcertosErros acertosVsErros;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvolucaoDesempenho {
        private String data;
        private Double taxaAcerto;
        private Double tempoMedio;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvolucaoRetencao {
        private String data;
        private Double retencao;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErroPorTema {
        private String tema;
        private Integer quantidade;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProbabilidadeEsquecimento {
        private String conteudo;
        private String pinyin;
        private Double probabilidade;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcertosErros {
        private Long acertos;
        private Long erros;
    }
}
