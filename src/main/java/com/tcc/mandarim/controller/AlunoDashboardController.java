package com.tcc.mandarim.controller;

import com.tcc.mandarim.entity.Usuario;
import com.tcc.mandarim.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/aluno")
@RequiredArgsConstructor
@Tag(name = "Aluno", description = "Dashboard pessoal do aluno")
public class AlunoDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard pessoal do aluno logado")
    public ResponseEntity<Map<String, Object>> dashboard(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(dashboardService.alunoDashboard(usuario.getId()));
    }
}
