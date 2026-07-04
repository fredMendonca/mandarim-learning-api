package com.tcc.mandarim.controller;

import com.tcc.mandarim.dto.request.ExercicioRequest;
import com.tcc.mandarim.dto.response.ExercicioResponse;
import com.tcc.mandarim.entity.enums.TipoExercicio;
import com.tcc.mandarim.service.ExercicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercicios")
@RequiredArgsConstructor
@Tag(name = "Exercícios", description = "Gerenciamento de exercícios")
public class ExercicioController {

    private final ExercicioService service;

    @PostMapping
    @Operation(summary = "Criar exercício")
    public ResponseEntity<ExercicioResponse> criar(@Valid @RequestBody ExercicioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @GetMapping
    @Operation(summary = "Listar exercícios com paginação")
    public ResponseEntity<Page<ExercicioResponse>> listarPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String tipo) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(service.listarPaginado(pageable, tipo));
    }

    @GetMapping("/todos")
    @Operation(summary = "Listar todos os exercícios (sem paginação)")
    public ResponseEntity<List<ExercicioResponse>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar exercício por ID")
    public ResponseEntity<ExercicioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Buscar exercícios por tipo")
    public ResponseEntity<List<ExercicioResponse>> buscarPorTipo(@PathVariable TipoExercicio tipo) {
        return ResponseEntity.ok(service.buscarPorTipo(tipo));
    }

    @GetMapping("/conteudo/{conteudoId}")
    @Operation(summary = "Buscar exercícios por conteúdo")
    public ResponseEntity<List<ExercicioResponse>> buscarPorConteudo(@PathVariable Long conteudoId) {
        return ResponseEntity.ok(service.buscarPorConteudo(conteudoId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar exercício")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
