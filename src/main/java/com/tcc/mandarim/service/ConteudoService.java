package com.tcc.mandarim.service;

import com.tcc.mandarim.dto.request.ConteudoRequest;
import com.tcc.mandarim.dto.response.ConteudoResponse;
import com.tcc.mandarim.entity.Conteudo;
import com.tcc.mandarim.entity.Tag;
import com.tcc.mandarim.entity.Tema;
import com.tcc.mandarim.entity.enums.TipoConteudo;
import com.tcc.mandarim.exception.ResourceNotFoundException;
import com.tcc.mandarim.mapper.ConteudoMapper;
import com.tcc.mandarim.repository.ConteudoRepository;
import com.tcc.mandarim.repository.TagRepository;
import com.tcc.mandarim.repository.TemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConteudoService {

    private final ConteudoRepository repository;
    private final TemaRepository temaRepository;
    private final TagRepository tagRepository;
    private final ConteudoMapper mapper;

    @Transactional
    public ConteudoResponse criar(ConteudoRequest request) {
        Conteudo entity = mapper.toEntity(request);

        if (request.getTemaIds() != null && !request.getTemaIds().isEmpty()) {
            Set<Tema> temas = new HashSet<>(temaRepository.findAllById(request.getTemaIds()));
            entity.setTemas(temas);
        }

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            entity.setTags(tags);
        }

        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<ConteudoResponse> listarTodos() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ConteudoResponse> listarPaginado(Pageable pageable, String tipo, Integer nivelHsk) {
        Page<Conteudo> page;

        if (tipo != null && !tipo.isBlank() && nivelHsk != null) {
            TipoConteudo tipoEnum = TipoConteudo.valueOf(tipo);
            page = repository.findByTipoAndNivelHsk(tipoEnum, nivelHsk, pageable);
        } else if (tipo != null && !tipo.isBlank()) {
            TipoConteudo tipoEnum = TipoConteudo.valueOf(tipo);
            page = repository.findByTipo(tipoEnum, pageable);
        } else if (nivelHsk != null) {
            page = repository.findByNivelHsk(nivelHsk, pageable);
        } else {
            page = repository.findAll(pageable);
        }

        return page.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ConteudoResponse buscarPorId(Long id) {
        Conteudo entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conteúdo", id));
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<ConteudoResponse> buscarPorTipo(TipoConteudo tipo) {
        return repository.findByTipo(tipo).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConteudoResponse> buscarPorNivelHsk(Integer nivel) {
        return repository.findByNivelHsk(nivel).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConteudoResponse atualizar(Long id, ConteudoRequest request) {
        Conteudo entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conteúdo", id));

        mapper.updateEntity(entity, request);

        if (request.getTemaIds() != null) {
            Set<Tema> temas = new HashSet<>(temaRepository.findAllById(request.getTemaIds()));
            entity.setTemas(temas);
        }

        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            entity.setTags(tags);
        }

        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Conteúdo", id);
        }
        repository.deleteById(id);
    }
}
