package com.tcc.mandarim.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "estatisticas_diarias", schema = "mandarim",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "data_referencia"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstatisticaDiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private Usuario usuario;

    @Column(name = "data_referencia", nullable = false)
    private LocalDate dataReferencia;

    @Column(name = "exercicios_realizados")
    @Builder.Default
    private Integer exerciciosRealizados = 0;

    @Builder.Default
    private Integer acertos = 0;

    @Builder.Default
    private Integer erros = 0;

    @Column(name = "taxa_acerto", precision = 5, scale = 2)
    private BigDecimal taxaAcerto;

    @Column(name = "tempo_estudo_segundos")
    @Builder.Default
    private Integer tempoEstudoSegundos = 0;

    @Column(name = "palavras_estudadas")
    @Builder.Default
    private Integer palavrasEstudadas = 0;

    @Column(name = "frases_estudadas")
    @Builder.Default
    private Integer frasesEstudadas = 0;
}
