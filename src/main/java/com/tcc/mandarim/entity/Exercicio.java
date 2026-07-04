package com.tcc.mandarim.entity;

import com.tcc.mandarim.entity.enums.TipoExercicio;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exercicios", schema = "mandarim")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conteudo_id", nullable = false)
    private Conteudo conteudo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoExercicio tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @Column(name = "resposta_esperada", columnDefinition = "TEXT")
    private String respostaEsperada;

    @Builder.Default
    private Short dificuldade = 1;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @OneToMany(mappedBy = "exercicio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Alternativa> alternativas = new ArrayList<>();
}
