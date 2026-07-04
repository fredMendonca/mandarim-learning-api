package com.tcc.mandarim.repository;

import com.tcc.mandarim.entity.ConteudoIa;
import com.tcc.mandarim.entity.enums.StatusIA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConteudoIaRepository extends JpaRepository<ConteudoIa, Long> {

    List<ConteudoIa> findByStatus(StatusIA status);

    List<ConteudoIa> findByUsuarioId(UUID usuarioId);

    List<ConteudoIa> findByUsuarioIdAndStatus(UUID usuarioId, StatusIA status);
}
