package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import java.util.Arrays;
import java.util.Locale;

/** Ciclo do atendimento de emergência: AGUARDANDO → INICIADO → CONCLUIDO, sem voltar atrás nem pular etapa. */
public enum SituacaoEmergencia {
    AGUARDANDO,
    INICIADO,
    CONCLUIDO;

    public boolean podeMudarPara(SituacaoEmergencia nova) {
        return switch (this) {
            case AGUARDANDO -> nova == INICIADO;
            case INICIADO -> nova == CONCLUIDO;
            case CONCLUIDO -> false;
        };
    }

    public static SituacaoEmergencia de(String valor) {
        try {
            return valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new RegraNegocioException("Situação de emergência inválida: '" + valor + "'. Valores aceitos: "
                    + Arrays.toString(values()) + ".");
        }
    }
}
