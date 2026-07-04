package com.tcc.mandarim.controller;

import com.tcc.mandarim.dto.response.EstatisticaDiariaResponse;
import com.tcc.mandarim.service.EstatisticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/estatisticas")
@RequiredArgsConstructor
@Tag(name = "Estatísticas", description = "Métricas e estatísticas de desempenho")
public class EstatisticaController {

    private final EstatisticaService service;

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Buscar todas as estatísticas do usuário")
    public ResponseEntity<List<EstatisticaDiariaResponse>> buscarPorUsuario(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(service.buscarPorUsuario(usuarioId));
    }

    @GetMapping("/usuario/{usuarioId}/periodo")
    @Operation(summary = "Buscar estatísticas do usuário por período")
    public ResponseEntity<List<EstatisticaDiariaResponse>> buscarPorPeriodo(
            @PathVariable UUID usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.buscarPorPeriodo(usuarioId, inicio, fim));
    }
}
