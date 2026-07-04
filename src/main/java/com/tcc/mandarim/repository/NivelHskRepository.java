package com.tcc.mandarim.repository;

import com.tcc.mandarim.entity.NivelHsk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NivelHskRepository extends JpaRepository<NivelHsk, Integer> {

    Optional<NivelHsk> findByNivel(Integer nivel);
}
