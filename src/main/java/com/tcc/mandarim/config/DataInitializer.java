package com.tcc.mandarim.config;

import com.tcc.mandarim.entity.Usuario;
import com.tcc.mandarim.entity.enums.Role;
import com.tcc.mandarim.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Cria ou atualiza usuário admin
        if (!usuarioRepository.existsByEmail("admin@mandarim.com")) {
            Usuario admin = Usuario.builder()
                    .nome("Administrador")
                    .email("admin@mandarim.com")
                    .senha(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .idiomaNativo("Português")
                    .nivelHskAtual(1)
                    .ativo(true)
                    .build();
            usuarioRepository.save(admin);
            log.info("[Init] Usuário admin criado: admin@mandarim.com / Admin@123");
        } else {
            // Sempre garante que o admin tem senha correta e role ADMIN
            usuarioRepository.findByEmail("admin@mandarim.com").ifPresent(admin -> {
                admin.setSenha(passwordEncoder.encode("Admin@123"));
                admin.setRole(Role.ADMIN);
                admin.setAtivo(true);
                usuarioRepository.save(admin);
                log.info("[Init] Admin atualizado com senha correta.");
            });
        }

        // Garante que todos os usuários existentes sem senha recebem uma senha padrão
        usuarioRepository.findAll().forEach(u -> {
            if (u.getSenha() == null || u.getSenha().isBlank()) {
                u.setSenha(passwordEncoder.encode("Aluno@123"));
                if (u.getRole() == null) u.setRole(Role.ALUNO);
                usuarioRepository.save(u);
                log.info("[Init] Senha padrão definida para: {} (Aluno@123)", u.getEmail());
            }
        });
    }
}
