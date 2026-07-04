package com.tcc.mandarim.repository;

import com.tcc.mandarim.entity.Alternativa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlternativaRepository extends JpaRepository<Alternativa, Long> {

    List<Alternativa> findByExercicioId(Long exercicioId);
}
