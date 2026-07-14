package com.tcc.mandarim.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RespostaValidatorTest {

    @Test
    @DisplayName("Deve aceitar resposta sem acento quando esperada tem acento")
    void deveAceitarSemAcento() {
        assertThat(RespostaValidator.isRespostaCorreta("onibus", "ônibus")).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar resposta com case diferente")
    void deveAceitarCaseDiferente() {
        assertThat(RespostaValidator.isRespostaCorreta("Ônibus", "ônibus")).isTrue();
        assertThat(RespostaValidator.isRespostaCorreta("ARROZ", "arroz")).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar qualquer opção quando há múltiplas separadas por vírgula")
    void deveAceitarMultiplasVirgula() {
        assertThat(RespostaValidator.isRespostaCorreta("legume", "legume, verdura")).isTrue();
        assertThat(RespostaValidator.isRespostaCorreta("verdura", "legume, verdura")).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar qualquer opção quando há múltiplas separadas por barra")
    void deveAceitarMultiplasBarra() {
        assertThat(RespostaValidator.isRespostaCorreta("comer", "comer/alimentar")).isTrue();
        assertThat(RespostaValidator.isRespostaCorreta("alimentar", "comer/alimentar")).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar pinyin sem tons")
    void deveAceitarPinyinSemTons() {
        assertThat(RespostaValidator.isRespostaCorreta("gong zuo", "gōng zuò")).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar pinyin sem tons - mǐfàn vs mifan")
    void deveAceitarPinyinSemTons2() {
        assertThat(RespostaValidator.isRespostaCorreta("mifan", "mǐfàn")).isTrue();
    }

    @Test
    @DisplayName("Deve rejeitar resposta vazia")
    void deveRejeitarVazia() {
        assertThat(RespostaValidator.isRespostaCorreta("", "água")).isFalse();
        assertThat(RespostaValidator.isRespostaCorreta("  ", "água")).isFalse();
        assertThat(RespostaValidator.isRespostaCorreta(null, "água")).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar resposta errada")
    void deveRejeitarErrada() {
        assertThat(RespostaValidator.isRespostaCorreta("casa", "água")).isFalse();
        assertThat(RespostaValidator.isRespostaCorreta("gato", "legume, verdura")).isFalse();
    }

    @Test
    @DisplayName("Deve tratar resposta esperada null")
    void deveTratarEsperadaNull() {
        assertThat(RespostaValidator.isRespostaCorreta("algo", null)).isFalse();
        assertThat(RespostaValidator.isRespostaCorreta("algo", "")).isFalse();
    }

    @Test
    @DisplayName("Normalize deve remover acentos e tons")
    void normalizeDeveRemoverAcentos() {
        assertThat(RespostaValidator.normalize("ônibus")).isEqualTo("onibus");
        assertThat(RespostaValidator.normalize("gōng zuò")).isEqualTo("gong zuo");
        assertThat(RespostaValidator.normalize("mǐfàn")).isEqualTo("mifan");
        assertThat(RespostaValidator.normalize("Água")).isEqualTo("agua");
    }
}
