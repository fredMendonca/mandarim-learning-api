package com.tcc.mandarim.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO legado mantido para compatibilidade.
 * Novos fluxos devem usar {@link GerarConteudoIaRequest}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConteudoIaRequest {

    private UUID usuarioId;
    private String prompt;
    private String respostaJson;
    private String observacao;
}
