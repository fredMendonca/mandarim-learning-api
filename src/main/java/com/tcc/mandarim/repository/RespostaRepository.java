package com.tcc.mandarim.repository;

import com.tcc.mandarim.entity.Resposta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RespostaRepository extends JpaRepository<Resposta, Long> {

    List<Resposta> findByUsuarioId(UUID usuarioId);

    List<Resposta> findByExercicioId(Long exercicioId);

    @Query("SELECT COUNT(r) FROM Resposta r WHERE r.usuarioId = :usuarioId")
    Long countByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Query("SELECT COUNT(r) FROM Resposta r WHERE r.usuarioId = :usuarioId AND r.correta = true")
    Long countAcertosByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Query("SELECT COUNT(r) FROM Resposta r WHERE r.usuarioId = :usuarioId AND r.correta = false")
    Long countErrosByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
