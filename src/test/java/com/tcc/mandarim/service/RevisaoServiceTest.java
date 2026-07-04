package com.tcc.mandarim.service;

import com.tcc.mandarim.dto.request.RevisaoRequest;
import com.tcc.mandarim.dto.response.RevisaoResponse;
import com.tcc.mandarim.entity.Conteudo;
import com.tcc.mandarim.entity.Revisao;
import com.tcc.mandarim.entity.enums.TipoConteudo;
import com.tcc.mandarim.mapper.RevisaoMapper;
import com.tcc.mandarim.repository.RevisaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevisaoServiceTest {

    @Mock
    private RevisaoRepository repository;

    @Mock
    private RevisaoMapper mapper;

    @InjectMocks
    private RevisaoService service;

    private UUID usuarioId;
    private Revisao revisao;
    private Conteudo conteudo;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();

        conteudo = Conteudo.builder()
                .id(1L)
                .tipo(TipoConteudo.PALAVRA)
                .hanzi("你好")
                .pinyin("nǐ hǎo")
                .traducao("Olá")
                .build();

        revisao = Revisao.builder()
                .id(1L)
                .usuarioId(usuarioId)
                .conteudoId(1L)
                .conteudo(conteudo)
                .intervaloDias(1)
                .facilidade(new BigDecimal("2.50"))
                .repeticoes(0)
                .lapsos(0)
                .proximaRevisao(LocalDate.now())
                .atualizadoEm(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve buscar revisões pendentes do usuário")
    void deveBuscarPendentes() {
        RevisaoResponse responseDto = RevisaoResponse.builder()
                .id(1L)
                .usuarioId(usuarioId)
                .conteudoId(1L)
                .hanzi("你好")
                .pinyin("nǐ hǎo")
                .traducao("Olá")
                .proximaRevisao(LocalDate.now())
                .build();

        when(repository.findPendentesByUsuarioId(eq(usuarioId), any(LocalDate.class)))
                .thenReturn(List.of(revisao));
        when(mapper.toResponse(revisao)).thenReturn(responseDto);

        List<RevisaoResponse> resultado = service.buscarPendentes(usuarioId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getHanzi()).isEqualTo("你好");
    }

    @Test
    @DisplayName("Deve atualizar revisão com qualidade alta (resposta correta)")
    void deveAtualizarRevisaoCorreta() {
        RevisaoRequest request = RevisaoRequest.builder().qualidade(5).build();

        RevisaoResponse responseDto = RevisaoResponse.builder()
                .id(1L)
                .usuarioId(usuarioId)
                .conteudoId(1L)
                .intervaloDias(1)
                .repeticoes(1)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(revisao));
        when(repository.save(any(Revisao.class))).thenReturn(revisao);
        when(mapper.toResponse(any(Revisao.class))).thenReturn(responseDto);

        RevisaoResponse resultado = service.atualizarRevisao(1L, request);

        assertThat(resultado).isNotNull();
        verify(repository, times(1)).save(any(Revisao.class));
    }

    @Test
    @DisplayName("Deve resetar revisão com qualidade baixa (resposta incorreta)")
    void deveResetarRevisaoIncorreta() {
        // Configura revisão com repetições anteriores
        revisao.setRepeticoes(3);
        revisao.setIntervaloDias(10);

        RevisaoRequest request = RevisaoRequest.builder().qualidade(1).build();

        RevisaoResponse responseDto = RevisaoResponse.builder()
                .id(1L)
                .repeticoes(0)
                .intervaloDias(1)
                .lapsos(1)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(revisao));
        when(repository.save(any(Revisao.class))).thenAnswer(invocation -> {
            Revisao saved = invocation.getArgument(0);
            assertThat(saved.getRepeticoes()).isEqualTo(0);
            assertThat(saved.getIntervaloDias()).isEqualTo(1);
            assertThat(saved.getLapsos()).isEqualTo(1);
            return saved;
        });
        when(mapper.toResponse(any(Revisao.class))).thenReturn(responseDto);

        service.atualizarRevisao(1L, request);

        verify(repository, times(1)).save(any(Revisao.class));
    }
}
