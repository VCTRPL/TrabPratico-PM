package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Cirurgia realizada por um {@link Cirurgiao}. A data e o horário herdados de {@link Atendimento}
 * são o início previsto; o término previsto e as datas efetivas ficam aqui.
 * O ciclo de vida segue AGENDADO → EM_ANDAMENTO → CONCLUIDO, com cancelamento possível antes da conclusão.
 */
@Entity
public class ProcedimentoCirurgico extends Atendimento {

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(nullable = false)
    private String tipoCirurgia;

    @Column(length = 2000)
    private String observacoesPreOperatorias;

    @Column(length = 2000)
    private String observacoesPosOperatorias;

    private LocalDateTime terminoPrevisto;

    private LocalDateTime inicioEfetivo;

    private LocalDateTime terminoEfetivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProcedimento status;

    protected ProcedimentoCirurgico() {
    }

    public ProcedimentoCirurgico(
            Cirurgiao cirurgiao,
            Paciente paciente,
            LocalDate dataPrevista,
            LocalTime horarioPrevisto,
            LocalDateTime terminoPrevisto,
            String descricao,
            String tipoCirurgia,
            String observacoesPreOperatorias) {
        super(paciente, cirurgiao, dataPrevista, horarioPrevisto);
        this.descricao = textoOuNulo(descricao);
        this.tipoCirurgia = textoOuNulo(tipoCirurgia);
        this.observacoesPreOperatorias = textoOuNulo(observacoesPreOperatorias);
        this.terminoPrevisto = terminoPrevisto;
        this.status = StatusProcedimento.AGENDADO;
        validar();
    }

    @Override
    public void validar() {
        if (descricao == null) {
            throw new RegraNegocioException("O procedimento cirúrgico exige uma descrição.");
        }
        if (tipoCirurgia == null) {
            throw new RegraNegocioException("O procedimento cirúrgico exige o tipo de cirurgia.");
        }
        if (terminoPrevisto != null && !terminoPrevisto.isAfter(getInicioPrevisto())) {
            throw new RegraNegocioException("O término previsto deve ser posterior ao início previsto.");
        }
    }

    @Override
    public boolean ocupaAgenda() {
        return status != StatusProcedimento.CANCELADO;
    }

    public void iniciar(LocalDateTime momento) {
        if (status != StatusProcedimento.AGENDADO) {
            throw new RegraNegocioException(
                    "Só é possível iniciar um procedimento agendado (status atual: " + status + ").");
        }
        this.inicioEfetivo = momento;
        this.status = StatusProcedimento.EM_ANDAMENTO;
    }

    public void concluir(LocalDateTime momento, String observacoesPosOperatorias) {
        if (status != StatusProcedimento.EM_ANDAMENTO) {
            throw new RegraNegocioException(
                    "Não é possível concluir um procedimento que não foi iniciado (status atual: " + status + ").");
        }
        this.terminoEfetivo = momento;
        this.observacoesPosOperatorias = textoOuNulo(observacoesPosOperatorias);
        this.status = StatusProcedimento.CONCLUIDO;
    }

    public void cancelar() {
        if (status == StatusProcedimento.CONCLUIDO || status == StatusProcedimento.CANCELADO) {
            throw new RegraNegocioException("Não é possível cancelar um procedimento com status " + status + ".");
        }
        this.status = StatusProcedimento.CANCELADO;
    }

    public LocalDateTime getInicioPrevisto() {
        return LocalDateTime.of(getData(), getHorario());
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    public String getDescricao() {
        return descricao;
    }

    public String getTipoCirurgia() {
        return tipoCirurgia;
    }

    public String getObservacoesPreOperatorias() {
        return observacoesPreOperatorias;
    }

    public String getObservacoesPosOperatorias() {
        return observacoesPosOperatorias;
    }

    public LocalDateTime getTerminoPrevisto() {
        return terminoPrevisto;
    }

    public LocalDateTime getInicioEfetivo() {
        return inicioEfetivo;
    }

    public LocalDateTime getTerminoEfetivo() {
        return terminoEfetivo;
    }

    public StatusProcedimento getStatus() {
        return status;
    }
}
