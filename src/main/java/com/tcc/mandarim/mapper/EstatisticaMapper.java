package com.tcc.mandarim.mapper;

import com.tcc.mandarim.dto.response.EstatisticaDiariaResponse;
import com.tcc.mandarim.entity.EstatisticaDiaria;
import org.springframework.stereotype.Component;

@Component
public class EstatisticaMapper {

    public EstatisticaDiariaResponse toResponse(EstatisticaDiaria entity) {
        return EstatisticaDiariaResponse.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .dataReferencia(entity.getDataReferencia())
                .exerciciosRealizados(entity.getExerciciosRealizados())
                .acertos(entity.getAcertos())
                .erros(entity.getErros())
                .taxaAcerto(entity.getTaxaAcerto())
                .tempoEstudoSegundos(entity.getTempoEstudoSegundos())
                .palavrasEstudadas(entity.getPalavrasEstudadas())
                .frasesEstudadas(entity.getFrasesEstudadas())
                .build();
    }
}
