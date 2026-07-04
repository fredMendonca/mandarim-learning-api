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
public class TemaRequest {

    @NotBlank(message = "Nome do tema é obrigatório")
    private String nome;

    private String descricao;
}
