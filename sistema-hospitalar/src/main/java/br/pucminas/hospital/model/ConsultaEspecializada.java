package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Consulta em uma especialidade. Só pode ser atendida por um {@link MedicoEspecialista} cuja área seja
 * compatível com a especialidade da consulta (a comparação ignora maiúsculas e acentos). As observações
 * médicas são herdadas de {@link Consulta}.
 */
@Entity
public class ConsultaEspecializada extends Consulta {

    @Column(nullable = false)
    private String especialidade;

    protected ConsultaEspecializada() {
    }

    public ConsultaEspecializada(
            Paciente paciente, ProfissionalSaude profissional, LocalDate data, LocalTime horario, String especialidade) {
        super(paciente, profissional, data, horario);
        this.especialidade = textoOuNulo(especialidade);
        validar();
    }

    @Override
    public void validar() {
        if (especialidade == null) {
            throw new RegraNegocioException("A consulta especializada exige a especialidade.");
        }
        if (!(getProfissional() instanceof MedicoEspecialista) || !getProfissional().atuaNaArea(especialidade)) {
            throw new RegraNegocioException("A área do profissional (" + getProfissional().getAreaAtuacao()
                    + ") não é compatível com a especialidade da consulta (" + especialidade + ").");
        }
    }

    @Override
    public TipoConsulta getTipo() {
        return TipoConsulta.ESPECIALIZADA;
    }

    public String getEspecialidade() {
        return especialidade;
    }
}
