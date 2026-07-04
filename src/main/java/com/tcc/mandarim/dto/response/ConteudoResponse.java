package com.tcc.mandarim.dto.response;

import com.tcc.mandarim.entity.enums.OrigemConteudo;
import com.tcc.mandarim.entity.enums.TipoConteudo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConteudoResponse {

    private Long id;
    private TipoConteudo tipo;
    private String hanzi;
    private String pinyin;
    private String traducao;
    private String explicacao;
    private Integer nivelHsk;
    private Short dificuldade;
    private OrigemConteudo origem;
    private LocalDateTime criadoEm;
    private List<TemaResponse> temas;
    private List<TagResponse> tags;
}
