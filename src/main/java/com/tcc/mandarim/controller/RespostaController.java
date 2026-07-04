package com.tcc.mandarim.controller;

import com.tcc.mandarim.dto.request.RespostaRequest;
import com.tcc.mandarim.dto.response.DesempenhoResponse;
import com.tcc.mandarim.dto.response.RespostaResponse;
import com.tcc.mandarim.service.RespostaService;
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
@RequestMapping("/api/respostas")
@RequiredArgsConstructor
@Tag(name = "Respostas", description = "Registro e consulta de respostas dos usuários")
public class RespostaController {

    private final RespostaService service;

    @PostMapping
    @Operation(summary = "Registrar resposta do usuário")
    public ResponseEntity<RespostaResponse> registrar(@Valid @RequestBody RespostaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(request));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Buscar respostas por usuário")
    public ResponseEntity<List<RespostaResponse>> buscarPorUsuario(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(service.buscarPorUsuario(usuarioId));
    }

    @GetMapping("/usuario/{usuarioId}/desempenho")
    @Operation(summary = "Calcular desempenho do usuário")
    public ResponseEntity<DesempenhoResponse> calcularDesempenho(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(service.calcularDesempenho(usuarioId));
    }
}
