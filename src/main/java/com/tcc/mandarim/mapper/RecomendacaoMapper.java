package com.tcc.mandarim.mapper;

import com.tcc.mandarim.dto.request.RecomendacaoRequest;
import com.tcc.mandarim.dto.response.RecomendacaoResponse;
import com.tcc.mandarim.entity.Recomendacao;
import org.springframework.stereotype.Component;

@Component
public class RecomendacaoMapper {

    public Recomendacao toEntity(RecomendacaoRequest request) {
        return Recomendacao.builder()
                .usuarioId(request.getUsuarioId())
                .conteudoId(request.getConteudoId())
                .motivo(request.getMotivo())
                .scorePrioridade(request.getScorePrioridade())
                .origem(request.getOrigem() != null ? request.getOrigem() : "REGRA")
                .build();
    }

    public RecomendacaoResponse toResponse(Recomendacao entity) {
        return RecomendacaoResponse.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .conteudoId(entity.getConteudoId())
                .motivo(entity.getMotivo())
                .scorePrioridade(entity.getScorePrioridade())
                .origem(entity.getOrigem())
                .criadaEm(entity.getCriadaEm())
                .visualizada(entity.getVisualizada())
                .build();
    }
}
