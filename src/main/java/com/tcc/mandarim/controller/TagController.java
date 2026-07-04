package com.tcc.mandarim.controller;

import com.tcc.mandarim.dto.request.TagRequest;
import com.tcc.mandarim.dto.response.TagResponse;
import com.tcc.mandarim.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Gerenciamento de tags")
public class TagController {

    private final TagService service;

    @PostMapping
    @Operation(summary = "Criar tag")
    public ResponseEntity<TagResponse> criar(@Valid @RequestBody TagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @GetMapping
    @Operation(summary = "Listar todas as tags")
    public ResponseEntity<List<TagResponse>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tag")
    public ResponseEntity<TagResponse> atualizar(@PathVariable Integer id,
                                                  @Valid @RequestBody TagRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar tag")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
