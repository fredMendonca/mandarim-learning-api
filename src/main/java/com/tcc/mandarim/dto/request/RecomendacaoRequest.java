package com.tcc.mandarim.dto.request;

import jakarta.validation.constraints.NotNull;
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
public class RecomendacaoRequest {

    @NotNull(message = "ID do usuário é obrigatório")
    private UUID usuarioId;

    private Long conteudoId;

    private String motivo;

    private BigDecimal scorePrioridade;

    private String origem;
}
