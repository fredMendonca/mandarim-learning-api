package com.tcc.mandarim.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GerarConteudoIaRequest {

    @NotBlank(message = "Tipo é obrigatório (PALAVRA, FRASE ou DIALOGO)")
    private String tipo;

    private String tema;

    @NotNull(message = "Nível HSK é obrigatório")
    @Min(value = 1, message = "Nível HSK mínimo é 1")
    @Max(value = 6, message = "Nível HSK máximo é 6")
    private Integer nivelHsk;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade mínima é 1")
    @Max(value = 20, message = "Quantidade máxima é 20")
    private Integer quantidade;

    @NotBlank(message = "Objetivo é obrigatório (VOCABULARIO, CONVERSACAO ou GRAMATICA)")
    private String objetivo;

    @Builder.Default
    private String idiomaTraduzao = "Português";

    private String usuarioId;
}
