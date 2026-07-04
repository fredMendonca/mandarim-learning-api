package com.tcc.mandarim.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios", schema = "mandarim")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(name = "idioma_nativo", length = 50)
    @Builder.Default
    private String idiomaNativo = "Português";

    @Column(name = "nivel_hsk_atual")
    @Builder.Default
    private Integer nivelHskAtual = 1;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @Builder.Default
    private Boolean ativo = true;
}
