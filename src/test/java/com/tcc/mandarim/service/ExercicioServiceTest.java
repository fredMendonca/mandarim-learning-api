package com.tcc.mandarim.service;

import com.tcc.mandarim.dto.request.AlternativaRequest;
import com.tcc.mandarim.dto.request.ExercicioRequest;
import com.tcc.mandarim.dto.response.AlternativaResponse;
import com.tcc.mandarim.dto.response.ExercicioResponse;
import com.tcc.mandarim.entity.Conteudo;
import com.tcc.mandarim.entity.Exercicio;
import com.tcc.mandarim.entity.enums.TipoConteudo;
import com.tcc.mandarim.entity.enums.TipoExercicio;
import com.tcc.mandarim.mapper.ExercicioMapper;
import com.tcc.mandarim.repository.ConteudoRepository;
import com.tcc.mandarim.repository.ExercicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExercicioServiceTest {

    @Mock
    private ExercicioRepository repository;

    @Mock
    private ConteudoRepository conteudoRepository;

    @Mock
    private ExercicioMapper mapper;

    @InjectMocks
    private ExercicioService service;

    private ExercicioRequest request;
    private Conteudo conteudo;
    private Exercicio exercicio;
    private ExercicioResponse response;

    @BeforeEach
    void setUp() {
        conteudo = Conteudo.builder()
                .id(1L)
                .tipo(TipoConteudo.PALAVRA)
                .hanzi("你好")
                .traducao("Olá")
                .build();

        request = ExercicioRequest.builder()
                .conteudoId(1L)
                .tipo(TipoExercicio.MULTIPLA_ESCOLHA)
                .enunciado("Qual a tradução de 你好?")
                .respostaEsperada("Olá")
                .dificuldade((short) 1)
                .alternativas(List.of(
                        AlternativaRequest.builder().texto("Olá").correta(true).build(),
                        AlternativaRequest.builder().texto("Tchau").correta(false).build(),
                        AlternativaRequest.builder().texto("Obrigado").correta(false).build()
                ))
                .build();

        exercicio = Exercicio.builder()
                .id(1L)
                .conteudo(conteudo)
                .tipo(TipoExercicio.MULTIPLA_ESCOLHA)
                .enunciado("Qual a tradução de 你好?")
                .respostaEsperada("Olá")
                .dificuldade((short) 1)
                .criadoEm(LocalDateTime.now())
                .alternativas(new ArrayList<>())
                .build();

        response = ExercicioResponse.builder()
                .id(1L)
                .conteudoId(1L)
                .tipo(TipoExercicio.MULTIPLA_ESCOLHA)
                .enunciado("Qual a tradução de 你好?")
                .respostaEsperada("Olá")
                .dificuldade((short) 1)
                .alternativas(List.of(
                        AlternativaResponse.builder().id(1L).texto("Olá").correta(true).build(),
                        AlternativaResponse.builder().id(2L).texto("Tchau").correta(false).build()
                ))
                .build();
    }

    @Test
    @DisplayName("Deve criar exercício com alternativas")
    void deveCriarExercicio() {
        when(conteudoRepository.findById(1L)).thenReturn(Optional.of(conteudo));
        when(mapper.toEntity(request, conteudo)).thenReturn(exercicio);
        when(repository.save(any(Exercicio.class))).thenReturn(exercicio);
        when(mapper.toResponse(exercicio)).thenReturn(response);

        ExercicioResponse resultado = service.criar(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTipo()).isEqualTo(TipoExercicio.MULTIPLA_ESCOLHA);
        assertThat(resultado.getConteudoId()).isEqualTo(1L);
        verify(repository, times(1)).save(any(Exercicio.class));
    }

    @Test
    @DisplayName("Deve buscar exercícios por tipo")
    void deveBuscarPorTipo() {
        when(repository.findByTipo(TipoExercicio.MULTIPLA_ESCOLHA)).thenReturn(List.of(exercicio));
        when(mapper.toResponse(exercicio)).thenReturn(response);

        List<ExercicioResponse> resultado = service.buscarPorTipo(TipoExercicio.MULTIPLA_ESCOLHA);

        assertThat(resultado).hasSize(1);
    }
}
