package com.tcc.mandarim.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarRespostaRevisaoRequest {

    @NotNull(message = "ID do usuário é obrigatório")
    private UUID usuarioId;

    @NotNull(message = "ID da revisão é obrigatório")
    private Long revisaoId;

    @NotBlank(message = "Resposta do usuário é obrigatória")
    private String respostaUsuario;

    private Integer tempoRespostaSegundos;
}
