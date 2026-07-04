package com.tcc.mandarim.controller;

import com.tcc.mandarim.dto.request.ConteudoRequest;
import com.tcc.mandarim.dto.response.ConteudoResponse;
import com.tcc.mandarim.entity.enums.TipoConteudo;
import com.tcc.mandarim.service.ConteudoService;
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
@RequestMapping("/api/conteudos")
@RequiredArgsConstructor
@Tag(name = "Conteúdos", description = "Gerenciamento de conteúdos (palavras, frases, diálogos)")
public class ConteudoController {

    private final ConteudoService service;

    @PostMapping
    @Operation(summary = "Criar conteúdo")
    public ResponseEntity<ConteudoResponse> criar(@Valid @RequestBody ConteudoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @GetMapping
    @Operation(summary = "Listar conteúdos com paginação")
    public ResponseEntity<Page<ConteudoResponse>> listarPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer nivelHsk) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(service.listarPaginado(pageable, tipo, nivelHsk));
    }

    @GetMapping("/todos")
    @Operation(summary = "Listar todos os conteúdos (sem paginação)")
    public ResponseEntity<List<ConteudoResponse>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar conteúdo por ID")
    public ResponseEntity<ConteudoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Buscar conteúdos por tipo (PALAVRA, FRASE, DIALOGO)")
    public ResponseEntity<List<ConteudoResponse>> buscarPorTipo(@PathVariable TipoConteudo tipo) {
        return ResponseEntity.ok(service.buscarPorTipo(tipo));
    }

    @GetMapping("/hsk/{nivel}")
    @Operation(summary = "Buscar conteúdos por nível HSK")
    public ResponseEntity<List<ConteudoResponse>> buscarPorNivelHsk(@PathVariable Integer nivel) {
        return ResponseEntity.ok(service.buscarPorNivelHsk(nivel));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar conteúdo")
    public ResponseEntity<ConteudoResponse> atualizar(@PathVariable Long id,
                                                       @Valid @RequestBody ConteudoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar conteúdo")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
