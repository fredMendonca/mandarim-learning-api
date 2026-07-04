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

    // Dados para gráficos
    private List<EvolucaoDiaria> evolucaoRetencao;
    private Map<String, Integer> revisoesPorPrioridade;
    private Map<String, Integer> errosPorTema;
    private AcertosErros acertosVsErros;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvolucaoDiaria {
        private String data;
        private Double taxaRetencao;
        private Integer revisoes;
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
