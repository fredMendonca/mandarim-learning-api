package com.tcc.mandarim.util;

import java.text.Normalizer;

/**
 * Utilitário para validação flexível de respostas.
 * Trata acentos, tons de pinyin, maiúsculas/minúsculas e múltiplas respostas.
 */
public final class RespostaValidator {

    private RespostaValidator() {}

    /**
     * Normaliza um texto removendo acentos, tons de pinyin,
     * convertendo para minúsculas e limpando espaços.
     */
    public static String normalize(String texto) {
        if (texto == null) return "";
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")  // Remove marcas diacríticas (acentos e tons)
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", " ")   // Normaliza espaços múltiplos
                .replaceAll("^[.!?¿¡]+", "") // Remove pontuação no início
                .replaceAll("[.!?]+$", "");   // Remove pontuação no final
        return normalizado.trim();
    }

    /**
     * Verifica se a resposta do usuário é correta comparando com a resposta esperada.
     * Suporta múltiplas respostas separadas por vírgula, ponto e vírgula ou barra.
     * A comparação ignora acentos, tons de pinyin e maiúsculas/minúsculas.
     */
    public static boolean isRespostaCorreta(String respostaUsuario, String respostaEsperada) {
        String usuario = normalize(respostaUsuario);
        if (usuario.isBlank()) return false;
        if (respostaEsperada == null || respostaEsperada.isBlank()) return false;

        String[] opcoes = respostaEsperada.split("[,;/]");
        for (String opcao : opcoes) {
            String opcaoNormalizada = normalize(opcao);
            if (!opcaoNormalizada.isBlank() && usuario.equals(opcaoNormalizada)) {
                return true;
            }
        }

        // Também verifica se a resposta esperada inteira (normalizada) é igual
        String esperadaCompleta = normalize(respostaEsperada);
        return usuario.equals(esperadaCompleta);
    }
}
