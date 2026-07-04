package com.tcc.mandarim.repository;

import com.tcc.mandarim.entity.Exercicio;
import com.tcc.mandarim.entity.enums.TipoExercicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExercicioRepository extends JpaRepository<Exercicio, Long> {

    List<Exercicio> findByTipo(TipoExercicio tipo);

    Page<Exercicio> findByTipo(TipoExercicio tipo, Pageable pageable);

    List<Exercicio> findByConteudoId(Long conteudoId);
}
