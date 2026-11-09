package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;
import java.time.LocalTime;

/** Atendimento de emergência, classificado por prioridade e com o ciclo AGUARDANDO → INICIADO → CONCLUIDO. */
@Entity
public class AtendimentoEmergencia extends Atendimento {

    @Column(nullable = false, length = 1000)
    private String queixa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadeEmergencia prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoEmergencia situacao;

    protected AtendimentoEmergencia() {
    }

    public AtendimentoEmergencia(
            Paciente paciente,
            ProfissionalSaude profissional,
            LocalDate data,
            LocalTime horario,
            String queixa,
            PrioridadeEmergencia prioridade) {
        super(paciente, profissional, data, horario);
        this.queixa = queixa == null || queixa.isBlank() ? null : queixa.trim();
        this.prioridade = prioridade;
        this.situacao = SituacaoEmergencia.AGUARDANDO;
        validar();
    }

    @Override
    public void validar() {
        if (queixa == null) {
            throw new RegraNegocioException("O atendimento de emergência exige a queixa do paciente.");
        }
        if (prioridade == null) {
            throw new RegraNegocioException("O atendimento de emergência exige uma prioridade.");
        }
    }

    public void alterarSituacao(SituacaoEmergencia nova) {
        if (nova == null) {
            throw new RegraNegocioException("A nova situação do atendimento é obrigatória.");
        }
        if (!situacao.podeMudarPara(nova)) {
            throw new RegraNegocioException("Transição inválida: o atendimento de emergência está " + situacao
                    + " e não pode passar para " + nova + ".");
        }
        this.situacao = nova;
    }

    public String getQueixa() {
        return queixa;
    }

    public PrioridadeEmergencia getPrioridade() {
        return prioridade;
    }

    public SituacaoEmergencia getSituacao() {
        return situacao;
    }
}
