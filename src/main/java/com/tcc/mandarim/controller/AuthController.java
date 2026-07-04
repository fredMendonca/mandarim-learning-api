package com.tcc.mandarim.controller;

import com.tcc.mandarim.dto.request.LoginRequest;
import com.tcc.mandarim.dto.request.RegisterRequest;
import com.tcc.mandarim.dto.response.AuthResponse;
import com.tcc.mandarim.entity.Usuario;
import com.tcc.mandarim.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login, registro e dados do usuário logado")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário e obter token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário (auto-registro como ALUNO)")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request, false));
    }

    @GetMapping("/me")
    @Operation(summary = "Obter dados do usuário logado")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(authService.me(usuario));
    }
}
