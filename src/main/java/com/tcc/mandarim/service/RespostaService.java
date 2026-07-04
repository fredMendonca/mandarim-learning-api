package com.tcc.mandarim.service;

import com.tcc.mandarim.dto.request.RespostaRequest;
import com.tcc.mandarim.dto.response.DesempenhoResponse;
import com.tcc.mandarim.dto.response.RespostaResponse;
import com.tcc.mandarim.entity.Resposta;
import com.tcc.mandarim.exception.ResourceNotFoundException;
import com.tcc.mandarim.mapper.RespostaMapper;
import com.tcc.mandarim.repository.ExercicioRepository;
import com.tcc.mandarim.repository.RespostaRepository;
import com.tcc.mandarim.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RespostaService {

    private final RespostaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final ExercicioRepository exercicioRepository;
    private final RespostaMapper mapper;

    @Transactional
    public RespostaResponse registrar(RespostaRequest request) {
        // Valida existência do usuário
        if (!usuarioRepository.existsById(request.getUsuarioId())) {
            throw new ResourceNotFoundException("Usuário", request.getUsuarioId());
        }

        // Valida existência do exercício
        if (!exercicioRepository.existsById(request.getExercicioId())) {
            throw new ResourceNotFoundException("Exercício", request.getExercicioId());
        }

        Resposta entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<RespostaResponse> buscarPorUsuario(UUID usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DesempenhoResponse calcularDesempenho(UUID usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuário", usuarioId);
        }

        Long total = repository.countByUsuarioId(usuarioId);
        Long acertos = repository.countAcertosByUsuarioId(usuarioId);
        Long erros = repository.countErrosByUsuarioId(usuarioId);

        BigDecimal taxaAcerto = BigDecimal.ZERO;
        if (total > 0) {
            taxaAcerto = BigDecimal.valueOf(acertos)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        }

        return DesempenhoResponse.builder()
                .usuarioId(usuarioId)
                .totalRespostas(total)
                .totalAcertos(acertos)
                .totalErros(erros)
                .taxaAcerto(taxaAcerto)
                .build();
    }
}
