package com.tcc.mandarim.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanoEstudoResponse {

    private String id;
    private String usuarioId;
    private String plano;
    private String dataCriacao;
}
