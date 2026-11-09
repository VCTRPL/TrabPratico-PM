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

@Entity
public class Encaminhamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "clinico_id", nullable = false)
    private MedicoClinicoGeral clinico;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(nullable = false)
    private String especialidadeDestino;

    @Column(nullable = false, length = 1000)
    private String motivo;

    @Column(nullable = false)
    private LocalDate dataEncaminhamento;

    protected Encaminhamento() {
    }

    /** Visível só no pacote: um encaminhamento nasce sempre de {@link MedicoClinicoGeral#encaminhar}. */
    Encaminhamento(MedicoClinicoGeral clinico, Paciente paciente, String especialidadeDestino, String motivo) {
        if (paciente == null) {
            throw new RegraNegocioException("O encaminhamento exige um paciente.");
        }
        if (especialidadeDestino == null || especialidadeDestino.isBlank()) {
            throw new RegraNegocioException("O encaminhamento exige a especialidade de destino.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("O encaminhamento exige um motivo.");
        }
        this.clinico = clinico;
        this.paciente = paciente;
        this.especialidadeDestino = especialidadeDestino.trim();
        this.motivo = motivo.trim();
        this.dataEncaminhamento = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public MedicoClinicoGeral getClinico() {
        return clinico;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public String getEspecialidadeDestino() {
        return especialidadeDestino;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDate getDataEncaminhamento() {
        return dataEncaminhamento;
    }
}
