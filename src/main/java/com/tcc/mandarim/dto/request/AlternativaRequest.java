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
public class AlternativaRequest {

    @NotBlank(message = "Texto da alternativa é obrigatório")
    private String texto;

    private Boolean correta;
}
