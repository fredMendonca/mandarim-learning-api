package com.tcc.mandarim.mapper;

import com.tcc.mandarim.dto.request.TagRequest;
import com.tcc.mandarim.dto.response.TagResponse;
import com.tcc.mandarim.entity.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public Tag toEntity(TagRequest request) {
        return Tag.builder()
                .nome(request.getNome())
                .build();
    }

    public TagResponse toResponse(Tag entity) {
        return TagResponse.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .build();
    }

    public void updateEntity(Tag entity, TagRequest request) {
        entity.setNome(request.getNome());
    }
}
