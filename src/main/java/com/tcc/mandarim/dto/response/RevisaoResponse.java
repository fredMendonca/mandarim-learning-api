package com.tcc.mandarim.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevisaoResponse {

    private Long id;
    private UUID usuarioId;
    private Long conteudoId;
    private String hanzi;
    private String pinyin;
    private String traducao;
    private LocalDateTime ultimaRevisao;
    private LocalDate proximaRevisao;
    private Integer intervaloDias;
    private BigDecimal facilidade;
    private Integer repeticoes;
    private Integer lapsos;
    private LocalDateTime atualizadoEm;
}
