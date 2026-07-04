package com.tcc.mandarim.dto.response;

import com.tcc.mandarim.entity.enums.TipoExercicio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExercicioResponse {

    private Long id;
    private Long conteudoId;
    private TipoExercicio tipo;
    private String enunciado;
    private String respostaEsperada;
    private Short dificuldade;
    private LocalDateTime criadoEm;
    private List<AlternativaResponse> alternativas;
}
