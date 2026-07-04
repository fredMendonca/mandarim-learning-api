package com.tcc.mandarim.service;

import com.tcc.mandarim.dto.request.ConteudoRequest;
import com.tcc.mandarim.dto.response.ConteudoResponse;
import com.tcc.mandarim.entity.Conteudo;
import com.tcc.mandarim.entity.enums.OrigemConteudo;
import com.tcc.mandarim.entity.enums.TipoConteudo;
import com.tcc.mandarim.mapper.ConteudoMapper;
import com.tcc.mandarim.repository.ConteudoRepository;
import com.tcc.mandarim.repository.TagRepository;
import com.tcc.mandarim.repository.TemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConteudoServiceTest {

    @Mock
    private ConteudoRepository repository;

    @Mock
    private TemaRepository temaRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ConteudoMapper mapper;

    @InjectMocks
    private ConteudoService service;

    private ConteudoRequest request;
    private Conteudo entity;
    private ConteudoResponse response;

    @BeforeEach
    void setUp() {
        request = ConteudoRequest.builder()
                .tipo(TipoConteudo.PALAVRA)
                .hanzi("你好")
                .pinyin("nǐ hǎo")
                .traducao("Olá")
                .explicacao("Cumprimento básico")
                .nivelHsk(1)
                .dificuldade((short) 1)
                .build();

        entity = Conteudo.builder()
                .id(1L)
                .tipo(TipoConteudo.PALAVRA)
                .hanzi("你好")
                .pinyin("nǐ hǎo")
                .traducao("Olá")
                .explicacao("Cumprimento básico")
                .nivelHsk(1)
                .dificuldade((short) 1)
                .origem(OrigemConteudo.MANUAL)
                .criadoEm(LocalDateTime.now())
                .temas(new HashSet<>())
                .tags(new HashSet<>())
                .estruturasGramaticais(new HashSet<>())
                .vocabulario(new HashSet<>())
                .build();

        response = ConteudoResponse.builder()
                .id(1L)
                .tipo(TipoConteudo.PALAVRA)
                .hanzi("你好")
                .pinyin("nǐ hǎo")
                .traducao("Olá")
                .explicacao("Cumprimento básico")
                .nivelHsk(1)
                .dificuldade((short) 1)
                .origem(OrigemConteudo.MANUAL)
                .temas(List.of())
                .tags(List.of())
                .build();
    }

    @Test
    @DisplayName("Deve criar conteúdo com sucesso")
    void deveCriarConteudo() {
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(any(Conteudo.class))).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        ConteudoResponse resultado = service.criar(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getHanzi()).isEqualTo("你好");
        assertThat(resultado.getTipo()).isEqualTo(TipoConteudo.PALAVRA);
        verify(repository, times(1)).save(any(Conteudo.class));
    }

    @Test
    @DisplayName("Deve buscar conteúdo por ID")
    void deveBuscarPorId() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        ConteudoResponse resultado = service.buscarPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve buscar conteúdos por tipo")
    void deveBuscarPorTipo() {
        when(repository.findByTipo(TipoConteudo.PALAVRA)).thenReturn(List.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        List<ConteudoResponse> resultado = service.buscarPorTipo(TipoConteudo.PALAVRA);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipo()).isEqualTo(TipoConteudo.PALAVRA);
    }

    @Test
    @DisplayName("Deve buscar conteúdos por nível HSK")
    void deveBuscarPorNivelHsk() {
        when(repository.findByNivelHsk(1)).thenReturn(List.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        List<ConteudoResponse> resultado = service.buscarPorNivelHsk(1);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNivelHsk()).isEqualTo(1);
    }
}
