package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

/**
 * As seis situações de uma consulta e as transições permitidas por {@link #podeMudarPara}:
 *
 * <pre>
 * AGENDADA     → CONFIRMADA, EM_ATENDIMENTO, CANCELADA, NAO_COMPARECEU
 * CONFIRMADA   → EM_ATENDIMENTO, CANCELADA, NAO_COMPARECEU
 * EM_ATENDIMENTO → CONCLUIDA
 * CONCLUIDA, CANCELADA, NAO_COMPARECEU → nenhuma
 * </pre>
 *
 * Uma consulta concluída nunca volta a ser agendada. Uma consulta cancelada (ou em que o paciente não
 * compareceu) só volta a andar com um novo agendamento ({@link Consulta#reagendar}), que a devolve para
 * AGENDADA; daí ela segue o fluxo normal até ser concluída.
 */
public enum SituacaoConsulta {
    AGENDADA,
    CONFIRMADA,
    EM_ATENDIMENTO,
    CONCLUIDA,
    CANCELADA,
    NAO_COMPARECEU;

    public Set<SituacaoConsulta> proximasPermitidas() {
        return switch (this) {
            case AGENDADA -> Set.of(CONFIRMADA, EM_ATENDIMENTO, CANCELADA, NAO_COMPARECEU);
            case CONFIRMADA -> Set.of(EM_ATENDIMENTO, CANCELADA, NAO_COMPARECEU);
            case EM_ATENDIMENTO -> Set.of(CONCLUIDA);
            case CONCLUIDA, CANCELADA, NAO_COMPARECEU -> Set.of();
        };
    }

    public boolean podeMudarPara(SituacaoConsulta nova) {
        return proximasPermitidas().contains(nova);
    }

    public boolean ehFinal() {
        return proximasPermitidas().isEmpty();
    }

    /** Situações a partir das quais a consulta pode receber um novo agendamento. */
    public boolean permiteReagendar() {
        return this != EM_ATENDIMENTO && this != CONCLUIDA;
    }

    /** Converte o texto recebido na API, aceitando maiúsculas ou minúsculas. */
    public static SituacaoConsulta de(String valor) {
        try {
            return valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new RegraNegocioException("Situação de consulta inválida: '" + valor + "'. Valores aceitos: "
                    + Arrays.toString(values()) + ".");
        }
    }
}
