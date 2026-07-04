package com.tcc.mandarim.service;

import com.tcc.mandarim.dto.response.EstatisticaDiariaResponse;
import com.tcc.mandarim.exception.ResourceNotFoundException;
import com.tcc.mandarim.mapper.EstatisticaMapper;
import com.tcc.mandarim.repository.EstatisticaDiariaRepository;
import com.tcc.mandarim.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstatisticaService {

    private final EstatisticaDiariaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final EstatisticaMapper mapper;

    @Transactional(readOnly = true)
    public List<EstatisticaDiariaResponse> buscarPorUsuario(UUID usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuário", usuarioId);
        }

        return repository.findByUsuarioIdOrderByDataReferenciaDesc(usuarioId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EstatisticaDiariaResponse> buscarPorPeriodo(UUID usuarioId, LocalDate inicio, LocalDate fim) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuário", usuarioId);
        }

        return repository.findByUsuarioIdAndPeriodo(usuarioId, inicio, fim).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}
