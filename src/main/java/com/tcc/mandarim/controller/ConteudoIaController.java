package com.tcc.mandarim.controller;

import com.tcc.mandarim.dto.request.GerarConteudoIaRequest;
import com.tcc.mandarim.dto.request.GerarExemplosRequest;
import com.tcc.mandarim.dto.request.GerarPlanoEstudoRequest;
import com.tcc.mandarim.dto.response.ConteudoIaResponse;
import com.tcc.mandarim.dto.response.PlanoEstudoResponse;
import com.tcc.mandarim.service.ConteudoIaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ia")
@RequiredArgsConstructor
@Tag(name = "IA", description = "Geração de conteúdo de mandarim via inteligência artificial")
public class ConteudoIaController {

    private final ConteudoIaService service;

    @PostMapping("/conteudos")
    @Operation(summary = "Gerar conteúdo estruturado via IA",
            description = "Recebe parâmetros de geração, chama o LLM e retorna conteúdos pendentes de aprovação")
    public ResponseEntity<ConteudoIaResponse> gerarConteudo(
            @Valid @RequestBody GerarConteudoIaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.gerarConteudo(request));
    }

    @GetMapping("/conteudos/pendentes")
    @Operation(summary = "Listar conteúdos IA pendentes de aprovação")
    public ResponseEntity<List<ConteudoIaResponse>> buscarPendentes() {
        return ResponseEntity.ok(service.buscarPendentes());
    }

    @PutMapping("/conteudos/{id}/aprovar")
    @Operation(summary = "Aprovar conteúdo gerado",
            description = "Salva os itens na tabela de conteúdos e gera exercícios automaticamente")
    public ResponseEntity<ConteudoIaResponse> aprovar(@PathVariable Long id) {
        return ResponseEntity.ok(service.aprovar(id));
    }

    @DeleteMapping("/conteudos/{id}")
    @Operation(summary = "Rejeitar/excluir conteúdo gerado")
    public ResponseEntity<Void> rejeitar(@PathVariable Long id) {
        service.rejeitar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/conteudos/exemplos")
    @Operation(summary = "Gerar exemplos adicionais para um conteúdo existente")
    public ResponseEntity<ConteudoIaResponse> gerarExemplos(
            @Valid @RequestBody GerarExemplosRequest request) {
        return ResponseEntity.ok(service.gerarExemplos(request));
    }

    @PostMapping("/plano-estudo")
    @Operation(summary = "Gerar plano de estudo personalizado",
            description = "Analisa o histórico do usuário e gera um plano de estudo semanal via IA")
    public ResponseEntity<PlanoEstudoResponse> gerarPlanoEstudo(
            @Valid @RequestBody GerarPlanoEstudoRequest request) {
        return ResponseEntity.ok(service.gerarPlanoEstudo(request));
    }
}
