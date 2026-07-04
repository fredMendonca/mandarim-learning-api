package com.tcc.mandarim.mapper;

import com.tcc.mandarim.dto.request.ExercicioRequest;
import com.tcc.mandarim.dto.response.AlternativaResponse;
import com.tcc.mandarim.dto.response.ExercicioResponse;
import com.tcc.mandarim.entity.Conteudo;
import com.tcc.mandarim.entity.Exercicio;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ExercicioMapper {

    public Exercicio toEntity(ExercicioRequest request, Conteudo conteudo) {
        return Exercicio.builder()
                .conteudo(conteudo)
                .tipo(request.getTipo())
                .enunciado(request.getEnunciado())
                .respostaEsperada(request.getRespostaEsperada())
                .dificuldade(request.getDificuldade() != null ? request.getDificuldade() : 1)
                .build();
    }

    public ExercicioResponse toResponse(Exercicio entity) {
        return ExercicioResponse.builder()
                .id(entity.getId())
                .conteudoId(entity.getConteudo().getId())
                .tipo(entity.getTipo())
                .enunciado(entity.getEnunciado())
                .respostaEsperada(entity.getRespostaEsperada())
                .dificuldade(entity.getDificuldade())
                .criadoEm(entity.getCriadoEm())
                .alternativas(entity.getAlternativas().stream()
                        .map(a -> AlternativaResponse.builder()
                                .id(a.getId())
                                .texto(a.getTexto())
                                .correta(a.getCorreta())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
