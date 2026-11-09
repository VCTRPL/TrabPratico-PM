package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Base de todo atendimento do hospital: sempre envolve um paciente, um profissional e um horário marcado.
 * Consultas e procedimentos cirúrgicos herdam desta classe, o que permite à agenda do profissional
 * consultar todos os atendimentos de uma só vez.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Atendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    private ProfissionalSaude profissional;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private LocalTime horario;

    protected Atendimento() {
    }

    protected Atendimento(Paciente paciente, ProfissionalSaude profissional, LocalDate data, LocalTime horario) {
        if (paciente == null) {
            throw new RegraNegocioException("O atendimento exige um paciente.");
        }
        if (profissional == null) {
            throw new RegraNegocioException("O atendimento exige um profissional responsável.");
        }
        if (data == null || horario == null) {
            throw new RegraNegocioException("O atendimento exige data e horário.");
        }
        this.paciente = paciente;
        this.profissional = profissional;
        this.data = data;
        this.horario = horario;
    }

    public Long getId() {
        return id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public ProfissionalSaude getProfissional() {
        return profissional;
    }

    public LocalDate getData() {
        return data;
    }

    public LocalTime getHorario() {
        return horario;
    }
}
