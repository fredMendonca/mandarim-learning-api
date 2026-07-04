package com.tcc.mandarim.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recomendacoes", schema = "mandarim")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recomendacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private Usuario usuario;

    @Column(name = "conteudo_id")
    private Long conteudoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conteudo_id", insertable = false, updatable = false)
    private Conteudo conteudo;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "score_prioridade", precision = 6, scale = 3)
    private BigDecimal scorePrioridade;

    @Column(length = 50)
    @Builder.Default
    private String origem = "REGRA";

    @CreationTimestamp
    @Column(name = "criada_em", updatable = false)
    private LocalDateTime criadaEm;

    @Builder.Default
    private Boolean visualizada = false;
}
