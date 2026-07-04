package com.tcc.mandarim.service;

import com.tcc.mandarim.dto.request.UsuarioRequest;
import com.tcc.mandarim.dto.response.UsuarioResponse;
import com.tcc.mandarim.entity.Usuario;
import com.tcc.mandarim.exception.BusinessException;
import com.tcc.mandarim.exception.ResourceNotFoundException;
import com.tcc.mandarim.mapper.UsuarioMapper;
import com.tcc.mandarim.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new BusinessException("E-mail já cadastrado: " + request.getEmail());
        }
        Usuario entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(UUID id) {
        Usuario entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
        return mapper.toResponse(entity);
    }

    @Transactional
    public UsuarioResponse atualizar(UUID id, UsuarioRequest request) {
        Usuario entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));

        // Verifica se o novo e-mail já existe para outro usuário
        repository.findByEmail(request.getEmail())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BusinessException("E-mail já cadastrado para outro usuário: " + request.getEmail());
                    }
                });

        mapper.updateEntity(entity, request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void deletar(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário", id);
        }
        repository.deleteById(id);
    }
}
