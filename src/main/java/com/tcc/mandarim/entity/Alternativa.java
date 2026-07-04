package com.tcc.mandarim.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alternativas", schema = "mandarim")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alternativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercicio_id", nullable = false)
    private Exercicio exercicio;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Builder.Default
    private Boolean correta = false;
}
