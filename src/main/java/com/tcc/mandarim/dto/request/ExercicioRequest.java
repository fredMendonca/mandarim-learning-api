package com.tcc.mandarim.dto.request;

import com.tcc.mandarim.entity.enums.TipoExercicio;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExercicioRequest {

    @NotNull(message = "ID do conteúdo é obrigatório")
    private Long conteudoId;

    @NotNull(message = "Tipo do exercício é obrigatório")
    private TipoExercicio tipo;

    @NotBlank(message = "Enunciado é obrigatório")
    private String enunciado;

    private String respostaEsperada;

    @Min(value = 1, message = "Dificuldade deve ser entre 1 e 5")
    @Max(value = 5, message = "Dificuldade deve ser entre 1 e 5")
    private Short dificuldade;

    private List<AlternativaRequest> alternativas;
}
