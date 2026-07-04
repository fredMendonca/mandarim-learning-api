package com.tcc.mandarim.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GerarPlanoEstudoRequest {

    @NotBlank(message = "ID do usuário é obrigatório")
    private String usuarioId;
}
