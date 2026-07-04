package com.tcc.mandarim.repository;

import com.tcc.mandarim.entity.EventoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventoUsuarioRepository extends JpaRepository<EventoUsuario, Long> {

    List<EventoUsuario> findByUsuarioIdOrderByCriadoEmDesc(UUID usuarioId);
}
