package com.tcc.mandarim.mapper;

import com.tcc.mandarim.dto.request.RespostaRequest;
import com.tcc.mandarim.dto.response.RespostaResponse;
import com.tcc.mandarim.entity.Resposta;
import org.springframework.stereotype.Component;

@Component
public class RespostaMapper {

    public Resposta toEntity(RespostaRequest request) {
        return Resposta.builder()
                .usuarioId(request.getUsuarioId())
                .sessaoId(request.getSessaoId())
                .exercicioId(request.getExercicioId())
                .respostaUsuario(request.getRespostaUsuario())
                .correta(request.getCorreta())
                .nota(request.getNota())
                .tempoRespostaSegundos(request.getTempoRespostaSegundos())
                .feedback(request.getFeedback())
                .feedbackIa(request.getFeedbackIa())
                .build();
    }

    public RespostaResponse toResponse(Resposta entity) {
        return RespostaResponse.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .sessaoId(entity.getSessaoId())
                .exercicioId(entity.getExercicioId())
                .respostaUsuario(entity.getRespostaUsuario())
                .correta(entity.getCorreta())
                .nota(entity.getNota())
                .tempoRespostaSegundos(entity.getTempoRespostaSegundos())
                .feedback(entity.getFeedback())
                .feedbackIa(entity.getFeedbackIa())
                .respondidoEm(entity.getRespondidoEm())
                .build();
    }
}
