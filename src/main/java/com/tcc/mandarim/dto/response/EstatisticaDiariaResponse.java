package com.tcc.mandarim.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticaDiariaResponse {

    private Long id;
    private UUID usuarioId;
    private LocalDate dataReferencia;
    private Integer exerciciosRealizados;
    private Integer acertos;
    private Integer erros;
    private BigDecimal taxaAcerto;
    private Integer tempoEstudoSegundos;
    private Integer palavrasEstudadas;
    private Integer frasesEstudadas;
}
