package com.tcc.mandarim.service;

import com.tcc.mandarim.dto.request.RevisaoRequest;
import com.tcc.mandarim.dto.response.RevisaoResponse;
import com.tcc.mandarim.entity.Revisao;
import com.tcc.mandarim.exception.ResourceNotFoundException;
import com.tcc.mandarim.mapper.RevisaoMapper;
import com.tcc.mandarim.repository.RevisaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevisaoService {

    private final RevisaoRepository repository;
    private final RevisaoMapper mapper;

    /**
     * Busca revisões pendentes (com proxima_revisao <= hoje).
     */
    @Transactional(readOnly = true)
    public List<RevisaoResponse> buscarPendentes(UUID usuarioId) {
        List<Revisao> pendentes = repository.findPendentesByUsuarioId(usuarioId, LocalDate.now());
        return pendentes.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza revisão espaçada usando algoritmo SM-2.
     * Qualidade: 0-5 (0 = falha total, 5 = resposta perfeita)
     */
    @Transactional
    public RevisaoResponse atualizarRevisao(Long id, RevisaoRequest request) {
        Revisao revisao = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Revisão", id));

        int qualidade = request.getQualidade();

        // Algoritmo SM-2
        BigDecimal facilidade = revisao.getFacilidade();
        int repeticoes = revisao.getRepeticoes();
        int intervaloDias = revisao.getIntervaloDias();
        int lapsos = revisao.getLapsos();

        if (qualidade < 3) {
            // Resposta incorreta - reset
            repeticoes = 0;
            intervaloDias = 1;
            lapsos++;
        } else {
            // Resposta correta
            if (repeticoes == 0) {
                intervaloDias = 1;
            } else if (repeticoes == 1) {
                intervaloDias = 6;
            } else {
                intervaloDias = (int) Math.round(intervaloDias * facilidade.doubleValue());
            }
            repeticoes++;
        }

        // Calcula nova facilidade: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        double novaFacilidade = facilidade.doubleValue()
                + (0.1 - (5 - qualidade) * (0.08 + (5 - qualidade) * 0.02));
        if (novaFacilidade < 1.3) {
            novaFacilidade = 1.3;
        }

        revisao.setFacilidade(BigDecimal.valueOf(novaFacilidade).setScale(2, RoundingMode.HALF_UP));
        revisao.setRepeticoes(repeticoes);
        revisao.setIntervaloDias(intervaloDias);
        revisao.setLapsos(lapsos);
        revisao.setUltimaRevisao(LocalDateTime.now());
        revisao.setProximaRevisao(LocalDate.now().plusDays(intervaloDias));
        revisao.setAtualizadoEm(LocalDateTime.now());

        revisao = repository.save(revisao);
        return mapper.toResponse(revisao);
    }
}
