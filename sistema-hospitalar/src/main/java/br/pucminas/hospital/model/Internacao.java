package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;

/** Internação de um paciente em um quarto. Exige paciente e quarto com vaga (RN4) e ocupa a vaga até a alta. */
@Entity
public class Internacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    private ProfissionalSaude profissionalResponsavel;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "quarto_id", nullable = false)
    private Quarto quarto;

    @Column(nullable = false)
    private LocalDate dataEntrada;

    private LocalDate dataPrevistaAlta;

    private LocalDate dataEfetivaAlta;

    @Column(length = 2000)
    private String observacoes;

    protected Internacao() {
    }

    public Internacao(
            Paciente paciente,
            ProfissionalSaude profissionalResponsavel,
            Quarto quarto,
            LocalDate dataEntrada,
            LocalDate dataPrevistaAlta,
            String observacoes) {
        if (paciente == null) {
            throw new RegraNegocioException("A internação exige um paciente.");
        }
        if (profissionalResponsavel == null) {
            throw new RegraNegocioException("A internação exige um profissional responsável.");
        }
        if (quarto == null) {
            throw new RegraNegocioException("A internação exige um quarto.");
        }
        if (dataEntrada == null) {
            throw new RegraNegocioException("A internação exige a data de entrada.");
        }
        this.paciente = paciente;
        this.profissionalResponsavel = profissionalResponsavel;
        this.dataEntrada = dataEntrada;
        atualizarPrevisao(dataPrevistaAlta, observacoes);
        quarto.ocuparVaga();
        this.quarto = quarto;
    }

    public final void atualizarPrevisao(LocalDate dataPrevistaAlta, String observacoes) {
        if (dataPrevistaAlta != null && dataPrevistaAlta.isBefore(dataEntrada)) {
            throw new RegraNegocioException("A data prevista de alta não pode ser anterior à data de entrada.");
        }
        this.dataPrevistaAlta = dataPrevistaAlta;
        this.observacoes = observacoes == null || observacoes.isBlank() ? null : observacoes.trim();
    }

    public void darAlta(LocalDate data) {
        if (!estaAtiva()) {
            throw new RegraNegocioException("Esta internação já recebeu alta em " + dataEfetivaAlta + ".");
        }
        if (data == null || data.isBefore(dataEntrada)) {
            throw new RegraNegocioException("A data de alta não pode ser anterior à data de entrada.");
        }
        this.dataEfetivaAlta = data;
        quarto.liberarVaga();
    }

    /** Chamado antes de excluir a internação, para não deixar a vaga presa no quarto. */
    public void liberarQuarto() {
        if (estaAtiva()) {
            quarto.liberarVaga();
        }
    }

    public boolean estaAtiva() {
        return dataEfetivaAlta == null;
    }

    public Long getId() {
        return id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public ProfissionalSaude getProfissionalResponsavel() {
        return profissionalResponsavel;
    }

    public Quarto getQuarto() {
        return quarto;
    }

    public LocalDate getDataEntrada() {
        return dataEntrada;
    }

    public LocalDate getDataPrevistaAlta() {
        return dataPrevistaAlta;
    }

    public LocalDate getDataEfetivaAlta() {
        return dataEfetivaAlta;
    }

    public String getObservacoes() {
        return observacoes;
    }
}
