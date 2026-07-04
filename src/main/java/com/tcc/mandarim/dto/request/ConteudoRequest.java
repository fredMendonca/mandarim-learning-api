package com.tcc.mandarim.dto.request;

import com.tcc.mandarim.entity.enums.OrigemConteudo;
import com.tcc.mandarim.entity.enums.TipoConteudo;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConteudoRequest {

    @NotNull(message = "Tipo do conteúdo é obrigatório")
    private TipoConteudo tipo;

    @NotBlank(message = "Hanzi é obrigatório")
    private String hanzi;

    private String pinyin;

    @NotBlank(message = "Tradução é obrigatória")
    private String traducao;

    private String explicacao;

    @Min(value = 1, message = "Nível HSK deve ser entre 1 e 6")
    @Max(value = 6, message = "Nível HSK deve ser entre 1 e 6")
    private Integer nivelHsk;

    @Min(value = 1, message = "Dificuldade deve ser entre 1 e 5")
    @Max(value = 5, message = "Dificuldade deve ser entre 1 e 5")
    private Short dificuldade;

    private OrigemConteudo origem;

    private List<Integer> temaIds;

    private List<Integer> tagIds;
}
