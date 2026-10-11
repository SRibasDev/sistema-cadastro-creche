package com.particaolar.mundo.system.support;

/**
 * Utilitário de normalização de dados de entrada.
 *
 * <p>Regra: todo dado que possua uma forma canónica (CPF, telefone, e-mail) deve ser
 * normalizado ANTES de ser persistido ou utilizado em verificações de unicidade.
 * Assim, {@code 529.982.247-25} e {@code 52998224725} são reconhecidos como o mesmo CPF.</p>
 *
 * <p>Não registra nem expõe dados pessoais.</p>
 */
public final class NormalizadorDados {

    private NormalizadorDados() {
    }

    /**
     * Remove qualquer caractere não numérico de um CPF, retornando apenas os 11 dígitos.
     *
     * @return CPF com 11 dígitos, ou {@code null} se a entrada for nula/vazia.
     */
    public static String normalizarCpf(String cpf) {
        if (cpf == null) {
            return null;
        }
        String digitos = cpf.replaceAll("\\D", "");
        return digitos.isEmpty() ? null : digitos;
    }

    /**
     * Remove qualquer caractere não numérico de um telefone (máscaras, espaços, parênteses).
     *
     * @return telefone apenas com dígitos, ou {@code null} se a entrada for nula/vazia.
     */
    public static String normalizarTelefone(String telefone) {
        if (telefone == null) {
            return null;
        }
        String digitos = telefone.replaceAll("\\D", "");
        return digitos.isEmpty() ? null : digitos;
    }

    /**
     * Normaliza um e-mail para minúsculas e sem espaços nas pontas.
     *
     * @return e-mail normalizado, ou {@code null} se a entrada for nula/vazia.
     */
    public static String normalizarEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalizado = email.trim().toLowerCase();
        return normalizado.isEmpty() ? null : normalizado;
    }
}
