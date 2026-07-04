package com.tcc.mandarim.service;

import com.tcc.mandarim.dto.request.RespostaRequest;
import com.tcc.mandarim.dto.response.DesempenhoResponse;
import com.tcc.mandarim.dto.response.RespostaResponse;
import com.tcc.mandarim.entity.Resposta;
import com.tcc.mandarim.mapper.RespostaMapper;
import com.tcc.mandarim.repository.ExercicioRepository;
import com.tcc.mandarim.repository.RespostaRepository;
import com.tcc.mandarim.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RespostaServiceTest {

    @Mock
    private RespostaRepository repository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ExercicioRepository exercicioRepository;

    @Mock
    private RespostaMapper mapper;

    @InjectMocks
    private RespostaService service;

    private UUID usuarioId;
    private RespostaRequest request;
    private Resposta entity;
    private RespostaResponse response;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();

        request = RespostaRequest.builder()
                .usuarioId(usuarioId)
                .exercicioId(1L)
                .respostaUsuario("Olá")
                .correta(true)
                .nota(BigDecimal.TEN)
                .tempoRespostaSegundos(15)
                .build();

        entity = Resposta.builder()
                .id(1L)
                .usuarioId(usuarioId)
                .exercicioId(1L)
                .respostaUsuario("Olá")
                .correta(true)
                .nota(BigDecimal.TEN)
                .tempoRespostaSegundos(15)
                .respondidoEm(LocalDateTime.now())
                .build();

        response = RespostaResponse.builder()
                .id(1L)
                .usuarioId(usuarioId)
                .exercicioId(1L)
                .respostaUsuario("Olá")
                .correta(true)
                .nota(BigDecimal.TEN)
                .tempoRespostaSegundos(15)
                .respondidoEm(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve registrar resposta com sucesso")
    void deveRegistrarResposta() {
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
        when(exercicioRepository.existsById(1L)).thenReturn(true);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(any(Resposta.class))).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        RespostaResponse resultado = service.registrar(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getCorreta()).isTrue();
        assertThat(resultado.getRespostaUsuario()).isEqualTo("Olá");
        verify(repository, times(1)).save(any(Resposta.class));
    }

    @Test
    @DisplayName("Deve calcular desempenho do usuário")
    void deveCalcularDesempenho() {
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
        when(repository.countByUsuarioId(usuarioId)).thenReturn(10L);
        when(repository.countAcertosByUsuarioId(usuarioId)).thenReturn(7L);
        when(repository.countErrosByUsuarioId(usuarioId)).thenReturn(3L);

        DesempenhoResponse resultado = service.calcularDesempenho(usuarioId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTotalRespostas()).isEqualTo(10L);
        assertThat(resultado.getTotalAcertos()).isEqualTo(7L);
        assertThat(resultado.getTotalErros()).isEqualTo(3L);
        assertThat(resultado.getTaxaAcerto()).isEqualByComparingTo(new BigDecimal("70.00"));
    }
}
