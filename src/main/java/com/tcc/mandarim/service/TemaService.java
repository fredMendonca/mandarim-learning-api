package com.tcc.mandarim.service;

import com.tcc.mandarim.dto.request.TemaRequest;
import com.tcc.mandarim.dto.response.TemaResponse;
import com.tcc.mandarim.entity.Tema;
import com.tcc.mandarim.exception.BusinessException;
import com.tcc.mandarim.exception.ResourceNotFoundException;
import com.tcc.mandarim.mapper.TemaMapper;
import com.tcc.mandarim.repository.TemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemaService {

    private final TemaRepository repository;
    private final TemaMapper mapper;

    @Transactional
    public TemaResponse criar(TemaRequest request) {
        if (repository.existsByNome(request.getNome())) {
            throw new BusinessException("Tema já existe: " + request.getNome());
        }
        Tema entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<TemaResponse> listarTodos() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TemaResponse atualizar(Integer id, TemaRequest request) {
        Tema entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tema", id));
        mapper.updateEntity(entity, request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void deletar(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Tema", id);
        }
        repository.deleteById(id);
    }
}
