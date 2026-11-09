package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Consulta de rotina: avaliação geral feita pelo {@link MedicoClinicoGeral}. Exige um motivo; as observações
 * médicas (herdadas de {@link Consulta}) só são registradas depois de realizada.
 */
@Entity
public class ConsultaRotina extends Consulta {

    @Column(nullable = false, length = 1000)
    private String motivo;

    protected ConsultaRotina() {
    }

    public ConsultaRotina(
            Paciente paciente, ProfissionalSaude profissional, LocalDate data, LocalTime horario, String motivo) {
        super(paciente, profissional, data, horario);
        this.motivo = textoOuNulo(motivo);
        validar();
    }

    @Override
    public void validar() {
        if (motivo == null) {
            throw new RegraNegocioException("A consulta de rotina exige um motivo.");
        }
        if (!(getProfissional() instanceof MedicoClinicoGeral)) {
            throw new RegraNegocioException("A consulta de rotina deve ser realizada por um médico clínico geral.");
        }
    }

    @Override
    public TipoConsulta getTipo() {
        return TipoConsulta.ROTINA;
    }

    public String getMotivo() {
        return motivo;
    }
}
