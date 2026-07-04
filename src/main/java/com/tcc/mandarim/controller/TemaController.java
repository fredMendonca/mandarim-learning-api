package com.tcc.mandarim.controller;

import com.tcc.mandarim.dto.request.TemaRequest;
import com.tcc.mandarim.dto.response.TemaResponse;
import com.tcc.mandarim.service.TemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/temas")
@RequiredArgsConstructor
@Tag(name = "Temas", description = "Gerenciamento de temas/categorias")
public class TemaController {

    private final TemaService service;

    @PostMapping
    @Operation(summary = "Criar tema")
    public ResponseEntity<TemaResponse> criar(@Valid @RequestBody TemaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @GetMapping
    @Operation(summary = "Listar todos os temas")
    public ResponseEntity<List<TemaResponse>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tema")
    public ResponseEntity<TemaResponse> atualizar(@PathVariable Integer id,
                                                   @Valid @RequestBody TemaRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar tema")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
