package com.tcc.mandarim.mapper;

import com.tcc.mandarim.dto.request.ConteudoRequest;
import com.tcc.mandarim.dto.response.ConteudoResponse;
import com.tcc.mandarim.dto.response.TagResponse;
import com.tcc.mandarim.dto.response.TemaResponse;
import com.tcc.mandarim.entity.Conteudo;
import com.tcc.mandarim.entity.enums.OrigemConteudo;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ConteudoMapper {

    public Conteudo toEntity(ConteudoRequest request) {
        return Conteudo.builder()
                .tipo(request.getTipo())
                .hanzi(request.getHanzi())
                .pinyin(request.getPinyin())
                .traducao(request.getTraducao())
                .explicacao(request.getExplicacao())
                .nivelHsk(request.getNivelHsk())
                .dificuldade(request.getDificuldade() != null ? request.getDificuldade() : 1)
                .origem(request.getOrigem() != null ? request.getOrigem() : OrigemConteudo.MANUAL)
                .build();
    }

    public ConteudoResponse toResponse(Conteudo entity) {
        return ConteudoResponse.builder()
                .id(entity.getId())
                .tipo(entity.getTipo())
                .hanzi(entity.getHanzi())
                .pinyin(entity.getPinyin())
                .traducao(entity.getTraducao())
                .explicacao(entity.getExplicacao())
                .nivelHsk(entity.getNivelHsk())
                .dificuldade(entity.getDificuldade())
                .origem(entity.getOrigem())
                .criadoEm(entity.getCriadoEm())
                .temas(entity.getTemas().stream()
                        .map(t -> TemaResponse.builder()
                                .id(t.getId())
                                .nome(t.getNome())
                                .descricao(t.getDescricao())
                                .build())
                        .collect(Collectors.toList()))
                .tags(entity.getTags().stream()
                        .map(t -> TagResponse.builder()
                                .id(t.getId())
                                .nome(t.getNome())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    public void updateEntity(Conteudo entity, ConteudoRequest request) {
        entity.setTipo(request.getTipo());
        entity.setHanzi(request.getHanzi());
        entity.setPinyin(request.getPinyin());
        entity.setTraducao(request.getTraducao());
        entity.setExplicacao(request.getExplicacao());
        entity.setNivelHsk(request.getNivelHsk());
        if (request.getDificuldade() != null) {
            entity.setDificuldade(request.getDificuldade());
        }
        if (request.getOrigem() != null) {
            entity.setOrigem(request.getOrigem());
        }
    }
}
