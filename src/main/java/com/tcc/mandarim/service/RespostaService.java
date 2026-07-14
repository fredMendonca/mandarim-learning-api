package com.tcc.mandarim.service;

import com.tcc.mandarim.dto.request.RespostaRequest;
import com.tcc.mandarim.dto.response.DesempenhoResponse;
import com.tcc.mandarim.dto.response.RespostaResponse;
import com.tcc.mandarim.entity.Exercicio;
import com.tcc.mandarim.entity.Resposta;
import com.tcc.mandarim.entity.Revisao;
import com.tcc.mandarim.exception.ResourceNotFoundException;
import com.tcc.mandarim.mapper.RespostaMapper;
import com.tcc.mandarim.repository.ExercicioRepository;
import com.tcc.mandarim.repository.RespostaRepository;
import com.tcc.mandarim.repository.RevisaoRepository;
import com.tcc.mandarim.repository.UsuarioRepository;
import com.tcc.mandarim.util.RespostaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RespostaService {

    private final RespostaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final ExercicioRepository exercicioRepository;
    private final RevisaoRepository revisaoRepository;
    private final RespostaMapper mapper;

    @Transactional
    public RespostaResponse registrar(RespostaRequest request) {
        // Valida existência do usuário
        if (!usuarioRepository.existsById(request.getUsuarioId())) {
            throw new ResourceNotFoundException("Usuário", request.getUsuarioId());
        }

        // Valida existência do exercício
        Exercicio exercicio = exercicioRepository.findById(request.getExercicioId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercício", request.getExercicioId()));

        // Validação server-side da resposta usando RespostaValidator
        boolean corretaValidada = RespostaValidator.isRespostaCorreta(
                request.getRespostaUsuario(),
                exercicio.getRespostaEsperada()
        );

        // Sobrescreve o valor enviado pelo frontend com a validação do servidor
        request.setCorreta(corretaValidada);
        if (corretaValidada) {
            request.setNota(new BigDecimal("100.00"));
        } else {
            request.setNota(BigDecimal.ZERO);
        }

        Resposta entity = mapper.toEntity(request);
        entity = repository.save(entity);

        // Cria revisão automática se ainda não existe para este conteúdo/usuário
        criarRevisaoSeNecessario(request.getUsuarioId(), request.getExercicioId(), corretaValidada);

        return mapper.toResponse(entity);
    }

    /**
     * Cria uma entrada na tabela revisoes se o usuário ainda não tem revisão
     * para o conteúdo associado a este exercício. Se já existe, atualiza com base na resposta.
     */
    private void criarRevisaoSeNecessario(UUID usuarioId, Long exercicioId, Boolean correta) {
        try {
            Exercicio exercicio = exercicioRepository.findById(exercicioId).orElse(null);
            if (exercicio == null || exercicio.getConteudo() == null) return;

            Long conteudoId = exercicio.getConteudo().getId();

            // Verifica se já existe revisão para este par usuário/conteúdo
            var existente = revisaoRepository.findByUsuarioIdAndConteudoId(usuarioId, conteudoId);

            if (existente.isEmpty()) {
                // Cria nova revisão com intervalo inicial baseado na resposta
                int intervaloDias = Boolean.TRUE.equals(correta) ? 1 : 1;
                LocalDate proximaRevisao = Boolean.TRUE.equals(correta)
                        ? LocalDate.now().plusDays(1)
                        : LocalDate.now(); // Se errou, revisão imediata

                Revisao revisao = Revisao.builder()
                        .usuarioId(usuarioId)
                        .conteudoId(conteudoId)
                        .ultimaRevisao(LocalDateTime.now())
                        .proximaRevisao(proximaRevisao)
                        .intervaloDias(intervaloDias)
                        .facilidade(new BigDecimal("2.50"))
                        .repeticoes(Boolean.TRUE.equals(correta) ? 1 : 0)
                        .lapsos(Boolean.TRUE.equals(correta) ? 0 : 1)
                        .atualizadoEm(LocalDateTime.now())
                        .build();

                revisaoRepository.save(revisao);
                log.debug("[Revisão Auto] Criada revisão para usuário={}, conteudo={}", usuarioId, conteudoId);
            } else {
                // Atualiza a revisão existente com base na nova resposta
                Revisao revisao = existente.get();
                revisao.setUltimaRevisao(LocalDateTime.now());
                revisao.setAtualizadoEm(LocalDateTime.now());

                if (Boolean.TRUE.equals(correta)) {
                    revisao.setRepeticoes(revisao.getRepeticoes() + 1);
                    int novoIntervalo = Math.max(1, (int) Math.round(
                            revisao.getIntervaloDias() * revisao.getFacilidade().doubleValue()));
                    revisao.setIntervaloDias(novoIntervalo);
                    revisao.setProximaRevisao(LocalDate.now().plusDays(novoIntervalo));
                } else {
                    revisao.setLapsos(revisao.getLapsos() + 1);
                    revisao.setIntervaloDias(1);
                    revisao.setProximaRevisao(LocalDate.now().plusDays(1));
                    BigDecimal novaFacilidade = revisao.getFacilidade().subtract(new BigDecimal("0.20"));
                    if (novaFacilidade.compareTo(new BigDecimal("1.30")) < 0) {
                        novaFacilidade = new BigDecimal("1.30");
                    }
                    revisao.setFacilidade(novaFacilidade);
                }

                revisaoRepository.save(revisao);
            }
        } catch (Exception e) {
            log.warn("[Revisão Auto] Erro ao criar/atualizar revisão: {}", e.getMessage());
        }
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
