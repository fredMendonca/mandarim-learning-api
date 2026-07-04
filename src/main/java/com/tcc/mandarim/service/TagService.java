package com.tcc.mandarim.service;

import com.tcc.mandarim.dto.request.TagRequest;
import com.tcc.mandarim.dto.response.TagResponse;
import com.tcc.mandarim.entity.Tag;
import com.tcc.mandarim.exception.BusinessException;
import com.tcc.mandarim.exception.ResourceNotFoundException;
import com.tcc.mandarim.mapper.TagMapper;
import com.tcc.mandarim.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository repository;
    private final TagMapper mapper;

    @Transactional
    public TagResponse criar(TagRequest request) {
        if (repository.existsByNome(request.getNome())) {
            throw new BusinessException("Tag já existe: " + request.getNome());
        }
        Tag entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<TagResponse> listarTodos() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TagResponse atualizar(Integer id, TagRequest request) {
        Tag entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        mapper.updateEntity(entity, request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void deletar(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Tag", id);
        }
        repository.deleteById(id);
    }
}
