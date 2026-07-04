package com.tcc.mandarim.entity;

import com.tcc.mandarim.entity.enums.OrigemConteudo;
import com.tcc.mandarim.entity.enums.TipoConteudo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "conteudos", schema = "mandarim")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conteudo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoConteudo tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String hanzi;

    @Column(columnDefinition = "TEXT")
    private String pinyin;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String traducao;

    @Column(columnDefinition = "TEXT")
    private String explicacao;

    @Column(name = "nivel_hsk")
    private Integer nivelHsk;

    @Builder.Default
    private Short dificuldade = 1;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private OrigemConteudo origem = OrigemConteudo.MANUAL;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @ManyToMany
    @JoinTable(
            name = "conteudo_temas",
            schema = "mandarim",
            joinColumns = @JoinColumn(name = "conteudo_id"),
            inverseJoinColumns = @JoinColumn(name = "tema_id")
    )
    @Builder.Default
    private Set<Tema> temas = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "conteudo_tags",
            schema = "mandarim",
            joinColumns = @JoinColumn(name = "conteudo_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "conteudo_gramatica",
            schema = "mandarim",
            joinColumns = @JoinColumn(name = "conteudo_id"),
            inverseJoinColumns = @JoinColumn(name = "gramatica_id")
    )
    @Builder.Default
    private Set<EstruturaGramatical> estruturasGramaticais = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "conteudo_vocabulario",
            schema = "mandarim",
            joinColumns = @JoinColumn(name = "frase_id"),
            inverseJoinColumns = @JoinColumn(name = "palavra_id")
    )
    @Builder.Default
    private Set<Conteudo> vocabulario = new HashSet<>();
}
