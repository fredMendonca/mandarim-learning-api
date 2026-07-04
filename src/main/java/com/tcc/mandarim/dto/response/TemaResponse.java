package com.tcc.mandarim.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemaResponse {

    private Integer id;
    private String nome;
    private String descricao;
}
