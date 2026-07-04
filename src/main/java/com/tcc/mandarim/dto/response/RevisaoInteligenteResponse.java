package com.tcc.mandarim.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevisaoInteligenteResponse {

    private Long revisaoId;
    private Long conteudoId;
    private String hanzi;
    private String pinyin;
    private String traducao;
    private Integer nivelHsk;
    private String tema;
    private String ultimaRevisao;
    private String proximaRevisao;
    private Integer repeticoes;
    private Integer lapsos;
    private Double taxaAcerto;
    private Double tempoMedioResposta;
    private Double probabilidadeEsquecimento;
    private Integer scorePrioridade;
    private String prioridade; // ALTA, MEDIA, BAIXA
    private String motivo;
}
