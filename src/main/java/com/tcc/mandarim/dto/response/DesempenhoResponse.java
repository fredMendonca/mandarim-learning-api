package com.tcc.mandarim.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesempenhoResponse {

    private UUID usuarioId;
    private Long totalRespostas;
    private Long totalAcertos;
    private Long totalErros;
    private BigDecimal taxaAcerto;
}
