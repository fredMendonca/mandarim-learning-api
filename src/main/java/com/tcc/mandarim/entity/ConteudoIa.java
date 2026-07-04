package com.tcc.mandarim.entity;

import com.tcc.mandarim.entity.enums.StatusIA;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conteudos_ia", schema = "mandarim")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConteudoIa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private Usuario usuario;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "parametros_json", columnDefinition = "TEXT")
    private String parametrosJson;

    @Column(name = "conteudos_gerados_json", columnDefinition = "TEXT")
    private String conteudosGeradosJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusIA status = StatusIA.PENDENTE;

    @Column(name = "tempo_processamento_ms")
    private Long tempoProcessamentoMs;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;
}
