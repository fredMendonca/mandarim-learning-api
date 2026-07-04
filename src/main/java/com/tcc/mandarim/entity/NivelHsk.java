package com.tcc.mandarim.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "niveis_hsk", schema = "mandarim")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NivelHsk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private Integer nivel;

    private String descricao;
}
