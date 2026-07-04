package com.tcc.mandarim.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "revisoes", schema = "mandarim",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "conteudo_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Revisao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private Usuario usuario;

    @Column(name = "conteudo_id", nullable = false)
    private Long conteudoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conteudo_id", insertable = false, updatable = false)
    private Conteudo conteudo;

    @Column(name = "ultima_revisao")
    private LocalDateTime ultimaRevisao;

    @Column(name = "proxima_revisao")
    private LocalDate proximaRevisao;

    @Column(name = "intervalo_dias")
    @Builder.Default
    private Integer intervaloDias = 1;

    @Column(precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal facilidade = new BigDecimal("2.50");

    @Builder.Default
    private Integer repeticoes = 0;

    @Builder.Default
    private Integer lapsos = 0;

    @Column(name = "atualizado_em")
    @Builder.Default
    private LocalDateTime atualizadoEm = LocalDateTime.now();
}
