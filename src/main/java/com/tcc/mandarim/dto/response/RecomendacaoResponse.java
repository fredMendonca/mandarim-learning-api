package com.tcc.mandarim.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecomendacaoResponse {

    private Long id;
    private UUID usuarioId;
    private Long conteudoId;
    private String motivo;
    private BigDecimal scorePrioridade;
    private String origem;
    private LocalDateTime criadaEm;
    private Boolean visualizada;
}
