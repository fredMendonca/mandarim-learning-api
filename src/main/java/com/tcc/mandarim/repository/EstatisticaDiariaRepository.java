package com.tcc.mandarim.repository;

import com.tcc.mandarim.entity.EstatisticaDiaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstatisticaDiariaRepository extends JpaRepository<EstatisticaDiaria, Long> {

    List<EstatisticaDiaria> findByUsuarioIdOrderByDataReferenciaDesc(UUID usuarioId);

    Optional<EstatisticaDiaria> findByUsuarioIdAndDataReferencia(UUID usuarioId, LocalDate dataReferencia);

    @Query("SELECT e FROM EstatisticaDiaria e WHERE e.usuarioId = :usuarioId " +
            "AND e.dataReferencia BETWEEN :inicio AND :fim ORDER BY e.dataReferencia")
    List<EstatisticaDiaria> findByUsuarioIdAndPeriodo(
            @Param("usuarioId") UUID usuarioId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim);
}
