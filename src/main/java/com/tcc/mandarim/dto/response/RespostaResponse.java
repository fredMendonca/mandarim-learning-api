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
public class RespostaResponse {

    private Long id;
    private UUID usuarioId;
    private Long sessaoId;
    private Long exercicioId;
    private String respostaUsuario;
    private Boolean correta;
    private BigDecimal nota;
    private Integer tempoRespostaSegundos;
    private String feedback;
    private String feedbackIa;
    private LocalDateTime respondidoEm;
}
