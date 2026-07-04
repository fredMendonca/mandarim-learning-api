package com.tcc.mandarim.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "temas", schema = "mandarim")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    private String descricao;
}
