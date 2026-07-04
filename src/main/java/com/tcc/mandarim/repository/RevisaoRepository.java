package com.tcc.mandarim.repository;

import com.tcc.mandarim.entity.Revisao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RevisaoRepository extends JpaRepository<Revisao, Long> {

    List<Revisao> findByUsuarioId(UUID usuarioId);

    @Query("SELECT r FROM Revisao r WHERE r.usuarioId = :usuarioId AND r.proximaRevisao <= :data")
    List<Revisao> findPendentesByUsuarioId(@Param("usuarioId") UUID usuarioId, @Param("data") LocalDate data);

    Optional<Revisao> findByUsuarioIdAndConteudoId(UUID usuarioId, Long conteudoId);
}
