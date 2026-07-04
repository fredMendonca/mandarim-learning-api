package com.tcc.mandarim.dto.request;

import jakarta.validation.constraints.*;
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
public class RespostaRequest {

    @NotNull(message = "ID do usuário é obrigatório")
    private UUID usuarioId;

    private Long sessaoId;

    @NotNull(message = "ID do exercício é obrigatório")
    private Long exercicioId;

    @NotBlank(message = "Resposta do usuário é obrigatória")
    private String respostaUsuario;

    private Boolean correta;

    @DecimalMin(value = "0.0", message = "Nota não pode ser negativa")
    @DecimalMax(value = "100.0", message = "Nota não pode ser maior que 100")
    private BigDecimal nota;

    @Min(value = 0, message = "Tempo de resposta não pode ser negativo")
    private Integer tempoRespostaSegundos;

    private String feedback;

    private String feedbackIa;
}
