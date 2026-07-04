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
public class GerarExemplosRequest {

    @NotNull(message = "ID do conteúdo é obrigatório")
    private Long conteudoId;

    @NotNull(message = "Nível HSK é obrigatório")
    @Min(1)
    @Max(6)
    private Integer nivelHsk;

    @Builder.Default
    private Integer quantidade = 3;
}
