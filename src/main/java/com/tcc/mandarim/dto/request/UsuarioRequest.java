package com.tcc.mandarim.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail deve ser válido")
    private String email;

    private String idiomaNativo;

    @Min(value = 1, message = "Nível HSK deve ser entre 1 e 6")
    @Max(value = 6, message = "Nível HSK deve ser entre 1 e 6")
    private Integer nivelHskAtual;
}
