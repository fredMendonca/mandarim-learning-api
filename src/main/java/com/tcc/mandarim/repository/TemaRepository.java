package com.tcc.mandarim.repository;

import com.tcc.mandarim.entity.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Integer> {

    Optional<Tema> findByNome(String nome);

    boolean existsByNome(String nome);
}
