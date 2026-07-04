package com.tcc.mandarim.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevisaoRequest {

    @NotNull(message = "Qualidade da resposta é obrigatória (0-5)")
    @Min(value = 0, message = "Qualidade deve ser entre 0 e 5")
    @Max(value = 5, message = "Qualidade deve ser entre 0 e 5")
    private Integer qualidade;
}
