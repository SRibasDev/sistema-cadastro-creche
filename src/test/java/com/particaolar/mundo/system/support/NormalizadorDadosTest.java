package com.particaolar.mundo.system.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Testes de regressão da normalização de dados pessoais.
 * Garantem que CPF/telefone/e-mail tenham forma canônica antes de persistir
 * e antes das verificações de unicidade.
 */
class NormalizadorDadosTest {

    @Test
    void deveRemoverMascaraDoCpf() {
        assertEquals("52998224725", NormalizadorDados.normalizarCpf("529.982.247-25"));
        assertEquals("52998224725", NormalizadorDados.normalizarCpf("52998224725"));
        assertEquals("52998224725", NormalizadorDados.normalizarCpf("529 982 247 25"));
    }

    @Test
    void cpfComFormatosDistintosDeveGerarMesmoResultado() {
        String a = NormalizadorDados.normalizarCpf("529.982.247-25");
        String b = NormalizadorDados.normalizarCpf("52998224725");
        assertEquals(a, b, "CPFs com máscara diferente devem normalizar para o mesmo valor");
    }

    @Test
    void deveRetornarNullParaCpfVazioOuNulo() {
        assertNull(NormalizadorDados.normalizarCpf(null));
        assertNull(NormalizadorDados.normalizarCpf(""));
        assertNull(NormalizadorDados.normalizarCpf("---"));
    }

    @Test
    void deveRemoverMascaraDoTelefone() {
        assertEquals("11988887777", NormalizadorDados.normalizarTelefone("(11) 98888-7777"));
        assertEquals("11988887777", NormalizadorDados.normalizarTelefone("11 98888 7777"));
        assertEquals("11988887777", NormalizadorDados.normalizarTelefone("11988887777"));
    }

    @Test
    void deveNormalizarEmailParaMinusculasSemEspacos() {
        assertEquals("admin@creche.com", NormalizadorDados.normalizarEmail("  Admin@Creche.com "));
        assertNull(NormalizadorDados.normalizarEmail("   "));
        assertNull(NormalizadorDados.normalizarEmail(null));
    }
}
