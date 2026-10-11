package com.particaolar.mundo.system.exception;

/**
 * Conflito de sobreposição: o pet já possui outra reserva ativa que intersecta
 * o intervalo solicitado. Intervalo é semifechado [entrada, saida), então reservas
 * adjacentes (saída igual à entrada seguinte) NÃO conflitam.
 */
public class ReservaConflitanteException extends RuntimeException {
    public ReservaConflitanteException(Long petId) {
        super("O pet " + petId + " já possui outra reserva ativa sobrepondo este período.");
    }
}
