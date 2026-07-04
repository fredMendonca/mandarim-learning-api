package com.tcc.mandarim.controller;

import com.tcc.mandarim.dto.request.RegistrarRespostaRevisaoRequest;
import com.tcc.mandarim.dto.request.RevisaoRequest;
import com.tcc.mandarim.dto.response.IndicadoresRevisaoResponse;
import com.tcc.mandarim.dto.response.RevisaoInteligenteResponse;
import com.tcc.mandarim.dto.response.RevisaoResponse;
import com.tcc.mandarim.service.RevisaoInteligenteService;
import com.tcc.mandarim.service.RevisaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/revisoes")
@RequiredArgsConstructor
@Tag(name = "Revisões", description = "Controle de revisão espaçada com BI e priorização inteligente")
public class RevisaoController {

    private final RevisaoService service;
    private final RevisaoInteligenteService inteligenteService;

    @GetMapping("/usuario/{usuarioId}/pendentes")
    @Operation(summary = "Buscar revisões pendentes do usuário")
    public ResponseEntity<List<RevisaoResponse>> buscarPendentes(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(service.buscarPendentes(usuarioId));
    }

    @GetMapping("/usuario/{usuarioId}/inteligentes")
    @Operation(summary = "Buscar revisões priorizadas com score de prioridade e probabilidade de esquecimento",
            description = "Retorna todas as revisões do usuário com cálculos de BI aplicados: score, prioridade, motivo, probabilidade de esquecimento")
    public ResponseEntity<List<RevisaoInteligenteResponse>> buscarInteligentes(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(inteligenteService.buscarRevisoesInteligentes(usuarioId));
    }

    @GetMapping("/usuario/{usuarioId}/indicadores")
    @Operation(summary = "Indicadores de desempenho do usuário para BI",
            description = "KPIs: revisões pendentes, taxa de retenção, conteúdos críticos, tempo médio, evolução diária, erros por tema")
    public ResponseEntity<IndicadoresRevisaoResponse> indicadores(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(inteligenteService.calcularIndicadores(usuarioId));
    }

    @PostMapping("/responder")
    @Operation(summary = "Registrar resposta de revisão prática",
            description = "Valida a resposta, registra na tabela respostas, atualiza revisão espaçada e recalcula próxima revisão")
    public ResponseEntity<RevisaoInteligenteResponse> registrarResposta(
            @Valid @RequestBody RegistrarRespostaRevisaoRequest request) {
        return ResponseEntity.ok(inteligenteService.registrarResposta(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar revisão espaçada (informar qualidade 0-5)")
    public ResponseEntity<RevisaoResponse> atualizar(@PathVariable Long id,
                                                      @Valid @RequestBody RevisaoRequest request) {
        return ResponseEntity.ok(service.atualizarRevisao(id, request));
    }
}
