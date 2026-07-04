package com.tcc.mandarim.service;

import com.tcc.mandarim.dto.request.ExercicioRequest;
import com.tcc.mandarim.dto.response.ExercicioResponse;
import com.tcc.mandarim.entity.Alternativa;
import com.tcc.mandarim.entity.Conteudo;
import com.tcc.mandarim.entity.Exercicio;
import com.tcc.mandarim.entity.enums.TipoExercicio;
import com.tcc.mandarim.exception.ResourceNotFoundException;
import com.tcc.mandarim.mapper.ExercicioMapper;
import com.tcc.mandarim.repository.ConteudoRepository;
import com.tcc.mandarim.repository.ExercicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExercicioService {

    private final ExercicioRepository repository;
    private final ConteudoRepository conteudoRepository;
    private final ExercicioMapper mapper;

    @Transactional
    public ExercicioResponse criar(ExercicioRequest request) {
        Conteudo conteudo = conteudoRepository.findById(request.getConteudoId())
                .orElseThrow(() -> new ResourceNotFoundException("Conteúdo", request.getConteudoId()));

        Exercicio exercicio = mapper.toEntity(request, conteudo);

        if (request.getAlternativas() != null && !request.getAlternativas().isEmpty()) {
            request.getAlternativas().forEach(altReq -> {
                Alternativa alternativa = Alternativa.builder()
                        .exercicio(exercicio)
                        .texto(altReq.getTexto())
                        .correta(altReq.getCorreta() != null ? altReq.getCorreta() : false)
                        .build();
                exercicio.getAlternativas().add(alternativa);
            });
        }

        Exercicio salvo = repository.save(exercicio);
        return mapper.toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<ExercicioResponse> listarTodos() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExercicioResponse buscarPorId(Long id) {
        Exercicio entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercício", id));
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<ExercicioResponse> buscarPorTipo(TipoExercicio tipo) {
        return repository.findByTipo(tipo).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExercicioResponse> buscarPorConteudo(Long conteudoId) {
        return repository.findByConteudoId(conteudoId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Exercício", id);
        }
        repository.deleteById(id);
    }
}
