package com.tcc.mandarim.mapper;

import com.tcc.mandarim.dto.request.UsuarioRequest;
import com.tcc.mandarim.dto.response.UsuarioResponse;
import com.tcc.mandarim.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toEntity(UsuarioRequest request) {
        return Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .idiomaNativo(request.getIdiomaNativo() != null ? request.getIdiomaNativo() : "Português")
                .nivelHskAtual(request.getNivelHskAtual() != null ? request.getNivelHskAtual() : 1)
                .build();
    }

    public UsuarioResponse toResponse(Usuario entity) {
        return UsuarioResponse.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .email(entity.getEmail())
                .idiomaNativo(entity.getIdiomaNativo())
                .nivelHskAtual(entity.getNivelHskAtual())
                .criadoEm(entity.getCriadoEm())
                .ativo(entity.getAtivo())
                .build();
    }

    public void updateEntity(Usuario entity, UsuarioRequest request) {
        entity.setNome(request.getNome());
        entity.setEmail(request.getEmail());
        if (request.getIdiomaNativo() != null) {
            entity.setIdiomaNativo(request.getIdiomaNativo());
        }
        if (request.getNivelHskAtual() != null) {
            entity.setNivelHskAtual(request.getNivelHskAtual());
        }
    }
}
