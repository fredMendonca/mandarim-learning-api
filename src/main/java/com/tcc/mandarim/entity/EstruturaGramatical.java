package com.tcc.mandarim.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estruturas_gramaticais", schema = "mandarim")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstruturaGramatical {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 150)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "exemplo_hanzi", columnDefinition = "TEXT")
    private String exemploHanzi;

    @Column(name = "exemplo_pinyin", columnDefinition = "TEXT")
    private String exemploPinyin;

    @Column(name = "exemplo_traducao", columnDefinition = "TEXT")
    private String exemploTraducao;
}
