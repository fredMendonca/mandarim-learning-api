package com.tcc.mandarim.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "respostas", schema = "mandarim")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private Usuario usuario;

    @Column(name = "sessao_id")
    private Long sessaoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessao_id", insertable = false, updatable = false)
    private SessaoEstudo sessao;

    @Column(name = "exercicio_id", nullable = false)
    private Long exercicioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercicio_id", insertable = false, updatable = false)
    private Exercicio exercicio;

    @Column(name = "resposta_usuario", nullable = false, columnDefinition = "TEXT")
    private String respostaUsuario;

    private Boolean correta;

    @Column(precision = 5, scale = 2)
    private BigDecimal nota;

    @Column(name = "tempo_resposta_segundos")
    private Integer tempoRespostaSegundos;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "feedback_ia", columnDefinition = "TEXT")
    private String feedbackIa;

    @CreationTimestamp
    @Column(name = "respondido_em", updatable = false)
    private LocalDateTime respondidoEm;
}
