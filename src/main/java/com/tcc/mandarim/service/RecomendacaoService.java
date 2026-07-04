package com.tcc.mandarim.service;

import com.tcc.mandarim.dto.request.RecomendacaoRequest;
import com.tcc.mandarim.dto.response.RecomendacaoResponse;
import com.tcc.mandarim.entity.Recomendacao;
import com.tcc.mandarim.exception.ResourceNotFoundException;
import com.tcc.mandarim.mapper.RecomendacaoMapper;
import com.tcc.mandarim.repository.RecomendacaoRepository;
import com.tcc.mandarim.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecomendacaoService {

    private final RecomendacaoRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final RecomendacaoMapper mapper;

    @Transactional
    public RecomendacaoResponse criar(RecomendacaoRequest request) {
        if (!usuarioRepository.existsById(request.getUsuarioId())) {
            throw new ResourceNotFoundException("Usuário", request.getUsuarioId());
        }

        Recomendacao entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<RecomendacaoResponse> buscarPorUsuario(UUID usuarioId) {
        return repository.findByUsuarioIdOrderByScorePrioridadeDesc(usuarioId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}
