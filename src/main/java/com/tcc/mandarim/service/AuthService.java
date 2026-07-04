package com.tcc.mandarim.service;

import com.tcc.mandarim.config.JwtUtil;
import com.tcc.mandarim.dto.request.LoginRequest;
import com.tcc.mandarim.dto.request.RegisterRequest;
import com.tcc.mandarim.dto.response.AuthResponse;
import com.tcc.mandarim.entity.Usuario;
import com.tcc.mandarim.entity.enums.Role;
import com.tcc.mandarim.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Email ou senha inválidos.");
        } catch (Exception e) {
            throw new RuntimeException("Erro na autenticação: " + e.getMessage(), e);
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email ou senha inválidos."));

        // Atualiza último login
        usuario.setUltimoLogin(LocalDateTime.now());
        usuarioRepository.save(usuario);

        // Garante que role não é null
        String roleName = usuario.getRole() != null ? usuario.getRole().name() : "ALUNO";

        String token = jwtUtil.generateToken(usuario, Map.of(
                "id", usuario.getId().toString(),
                "nome", usuario.getNome(),
                "role", roleName
        ));

        return AuthResponse.builder()
                .token(token)
                .id(usuario.getId().toString())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .role(roleName)
                .nivelHskAtual(usuario.getNivelHskAtual())
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, boolean isAdminCreating) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado: " + request.getEmail());
        }

        Role role = Role.ALUNO;
        if (isAdminCreating && request.getRole() != null) {
            try {
                role = Role.valueOf(request.getRole());
            } catch (IllegalArgumentException ignored) {}
        }

        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(role)
                .idiomaNativo(request.getIdiomaNativo() != null ? request.getIdiomaNativo() : "Português")
                .nivelHskAtual(request.getNivelHskAtual() != null ? request.getNivelHskAtual() : 1)
                .ativo(true)
                .build();

        usuario = usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuario, Map.of(
                "id", usuario.getId().toString(),
                "nome", usuario.getNome(),
                "role", usuario.getRole().name()
        ));

        return AuthResponse.builder()
                .token(token)
                .id(usuario.getId().toString())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .role(usuario.getRole().name())
                .nivelHskAtual(usuario.getNivelHskAtual())
                .build();
    }

    public AuthResponse me(Usuario usuario) {
        return AuthResponse.builder()
                .id(usuario.getId().toString())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .role(usuario.getRole().name())
                .nivelHskAtual(usuario.getNivelHskAtual())
                .build();
    }
}
