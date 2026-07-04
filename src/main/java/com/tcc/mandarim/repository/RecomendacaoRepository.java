package com.tcc.mandarim.repository;

import com.tcc.mandarim.entity.Recomendacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecomendacaoRepository extends JpaRepository<Recomendacao, Long> {

    List<Recomendacao> findByUsuarioIdOrderByScorePrioridadeDesc(UUID usuarioId);

    List<Recomendacao> findByUsuarioIdAndVisualizadaFalse(UUID usuarioId);
}
