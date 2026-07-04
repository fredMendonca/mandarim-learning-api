package com.tcc.mandarim.controller;

import com.tcc.mandarim.dto.request.RegisterRequest;
import com.tcc.mandarim.dto.response.AuthResponse;
import com.tcc.mandarim.entity.Usuario;
import com.tcc.mandarim.repository.UsuarioRepository;
import com.tcc.mandarim.service.AuthService;
import com.tcc.mandarim.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Painel administrativo (requer ROLE_ADMIN)")
public class AdminController {

    private final DashboardService dashboardService;
    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard administrativo com métricas globais")
    public ResponseEntity<Map<String, Object>> dashboard() {
        return ResponseEntity.ok(dashboardService.adminDashboard());
    }

    @GetMapping("/usuarios")
    @Operation(summary = "Listar todos os usuários")
    public ResponseEntity<List<Map<String, Object>>> listarUsuarios() {
        List<Map<String, Object>> usuarios = usuarioRepository.findAll().stream()
                .map(u -> {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("id", u.getId().toString());
                    m.put("nome", u.getNome());
                    m.put("email", u.getEmail());
                    m.put("role", u.getRole().name());
                    m.put("nivelHskAtual", u.getNivelHskAtual());
                    m.put("ativo", u.getAtivo());
                    m.put("criadoEm", u.getCriadoEm() != null ? u.getCriadoEm().toString() : null);
                    m.put("ultimoLogin", u.getUltimoLogin() != null ? u.getUltimoLogin().toString() : null);
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    @PostMapping("/usuarios")
    @Operation(summary = "Criar novo usuário (admin pode definir role)")
    public ResponseEntity<AuthResponse> criarUsuario(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request, true));
    }

    @PatchMapping("/usuarios/{id}/status")
    @Operation(summary = "Ativar ou inativar usuário")
    public ResponseEntity<Map<String, Object>> alterarStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> body) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Boolean ativo = body.get("ativo");
        if (ativo != null) {
            usuario.setAtivo(ativo);
            usuarioRepository.save(usuario);
        }
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("id", usuario.getId().toString());
        result.put("ativo", usuario.getAtivo());
        return ResponseEntity.ok(result);
    }
}
