package com.tcc.mandarim.controller;

import com.tcc.mandarim.dto.request.RecomendacaoRequest;
import com.tcc.mandarim.dto.response.RecomendacaoResponse;
import com.tcc.mandarim.service.RecomendacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recomendacoes")
@RequiredArgsConstructor
@Tag(name = "Recomendações", description = "Recomendações personalizadas de estudo")
public class RecomendacaoController {

    private final RecomendacaoService service;

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
}
