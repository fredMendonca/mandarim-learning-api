package com.tcc.mandarim.mapper;

import com.tcc.mandarim.dto.request.TemaRequest;
import com.tcc.mandarim.dto.response.TemaResponse;
import com.tcc.mandarim.entity.Tema;
import org.springframework.stereotype.Component;

@Component
public class TemaMapper {

    public Tema toEntity(TemaRequest request) {
        return Tema.builder()
                .nome(request.getNome())
                .descricao(request.getDescricao())
                .build();
    }

    public TemaResponse toResponse(Tema entity) {
        return TemaResponse.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .descricao(entity.getDescricao())
                .build();
    }

    public void updateEntity(Tema entity, TemaRequest request) {
        entity.setNome(request.getNome());
        entity.setDescricao(request.getDescricao());
    }
}
