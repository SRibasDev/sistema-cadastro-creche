package com.particaolar.mundo.system.util;

/**
 * Normalização de documentos e telefones.
 * Armazenamos SEMPRE sem máscara (apenas dígitos) para garantir unicidade
 * correta (o banco compara texto exato). Aceitamos máscara na entrada.
 */
public final class Documentos {

    private Documentos() {
    }

    /** Remove tudo que não for dígito. */
    public static String somenteDigitos(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.replaceAll("\\D", "");
    }

    /**
     * Normaliza CPF para 11 dígitos (sem pontuação).
     * Retorna null se não tiver 11 dígitos (a validação de formato
     * acontece antes, via @CPF).
     */
    public static String normalizarCpf(String cpf) {
        return somenteDigitos(cpf);
    }

    /**
     * Normaliza telefone para 10 ou 11 dígitos (sem máscara).
     */
    public static String normalizarTelefone(String telefone) {
        return somenteDigitos(telefone);
    }
}
