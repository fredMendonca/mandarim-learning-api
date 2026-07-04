package com.tcc.mandarim.mapper;

import com.tcc.mandarim.dto.response.RevisaoResponse;
import com.tcc.mandarim.entity.Revisao;
import org.springframework.stereotype.Component;

@Component
public class RevisaoMapper {

    public RevisaoResponse toResponse(Revisao entity) {
        RevisaoResponse.RevisaoResponseBuilder builder = RevisaoResponse.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .conteudoId(entity.getConteudoId())
                .ultimaRevisao(entity.getUltimaRevisao())
                .proximaRevisao(entity.getProximaRevisao())
                .intervaloDias(entity.getIntervaloDias())
                .facilidade(entity.getFacilidade())
                .repeticoes(entity.getRepeticoes())
                .lapsos(entity.getLapsos())
                .atualizadoEm(entity.getAtualizadoEm());

        if (entity.getConteudo() != null) {
            builder.hanzi(entity.getConteudo().getHanzi())
                    .pinyin(entity.getConteudo().getPinyin())
                    .traducao(entity.getConteudo().getTraducao());
        }

        return builder.build();
    }
}
