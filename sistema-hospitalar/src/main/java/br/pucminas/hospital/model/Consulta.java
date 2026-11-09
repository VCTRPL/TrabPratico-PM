package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Consulta agendada (de rotina ou especializada). Concentra o que as duas têm em comum: o ciclo de vida das
 * seis situações de {@link SituacaoConsulta}, o reagendamento e o registro das observações médicas.
 */
@Entity
public abstract class Consulta extends Atendimento {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoConsulta situacao;

    @Column(length = 2000)
    private String observacoesMedicas;

    protected Consulta() {
    }

    protected Consulta(Paciente paciente, ProfissionalSaude profissional, LocalDate data, LocalTime horario) {
        super(paciente, profissional, data, horario);
        this.situacao = SituacaoConsulta.AGENDADA;
    }

    public abstract TipoConsulta getTipo();

    public void alterarSituacao(SituacaoConsulta nova) {
        if (nova == null) {
            throw new RegraNegocioException("A nova situação da consulta é obrigatória.");
        }
        if (!situacao.podeMudarPara(nova)) {
            String dica = situacao.permiteReagendar() && nova != SituacaoConsulta.CANCELADA
                    ? " Para retomá-la é preciso um novo agendamento."
                    : "";
            throw new RegraNegocioException(situacao.ehFinal()
                    ? "A consulta está " + situacao + " e não pode passar para " + nova + "." + dica
                    : "Transição inválida: a consulta está " + situacao + " e não pode passar para " + nova
                            + ". Permitidas: " + situacao.proximasPermitidas() + ".");
        }
        this.situacao = nova;
    }

    public void cancelar() {
        alterarSituacao(SituacaoConsulta.CANCELADA);
    }

    /**
     * Marca a consulta para uma nova data e horário e a devolve para AGENDADA. Vale para consultas ainda
     * não iniciadas e para as canceladas ou em que o paciente não compareceu; nunca para uma concluída.
     */
    public void reagendar(LocalDate novaData, LocalTime novoHorario) {
        if (!situacao.permiteReagendar()) {
            throw new RegraNegocioException("Uma consulta " + situacao + " não pode ser reagendada.");
        }
        remarcar(novaData, novoHorario);
        this.situacao = SituacaoConsulta.AGENDADA;
    }

    /** As observações médicas só são registradas depois que a consulta foi realizada. */
    public void registrarObservacoes(String texto) {
        if (situacao != SituacaoConsulta.CONCLUIDA) {
            throw new RegraNegocioException(
                    "As observações só podem ser registradas após a realização da consulta (situação atual: "
                            + situacao + ").");
        }
        String observacoes = textoOuNulo(texto);
        if (observacoes == null) {
            throw new RegraNegocioException("As observações não podem ser vazias.");
        }
        this.observacoesMedicas = observacoes;
    }

    @Override
    public boolean ocupaAgenda() {
        return situacao != SituacaoConsulta.CANCELADA;
    }

    public SituacaoConsulta getSituacao() {
        return situacao;
    }

    public String getObservacoesMedicas() {
        return observacoesMedicas;
    }

    protected static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
