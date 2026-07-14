package com.tcc.mandarim.controller;

import com.tcc.mandarim.dto.request.RecomendacaoRequest;
import com.tcc.mandarim.dto.response.RecomendacaoResponse;
import com.tcc.mandarim.service.RecomendacaoService;
import com.tcc.mandarim.service.RecomendacaoInteligenteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/recomendacoes")
@RequiredArgsConstructor
@Tag(name = "Recomendações", description = "Recomendações personalizadas de estudo com BI e IA")
public class RecomendacaoController {

    private final RecomendacaoService service;
    private final RecomendacaoInteligenteService inteligenteService;

    @PostMapping
    @Operation(summary = "Criar recomendação de estudo")
    public ResponseEntity<RecomendacaoResponse> criar(@Valid @RequestBody RecomendacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Buscar recomendações do usuário")
    public ResponseEntity<List<RecomendacaoResponse>> buscarPorUsuario(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(service.buscarPorUsuario(usuarioId));
    }

    @GetMapping("/usuario/{usuarioId}/plano-inteligente")
    @Operation(summary = "Plano inteligente de estudo baseado em BI",
            description = "Retorna recomendações calculadas dinamicamente a partir do desempenho do aluno")
    public ResponseEntity<Map<String, Object>> planoInteligente(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(inteligenteService.gerarPlanoInteligente(usuarioId));
    }
}
