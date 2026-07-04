package com.tcc.mandarim.mapper;

import org.springframework.stereotype.Component;

/**
 * Mapper mantido para compatibilidade.
 * A lógica de conversão entity→response agora está em ConteudoIaService
 * pois envolve desserialização JSON dos campos parametrosJson e conteudosGeradosJson.
 */
@Component
public class ConteudoIaMapper {
    // Intencionalmente vazio — mapeamento feito diretamente no service
}
