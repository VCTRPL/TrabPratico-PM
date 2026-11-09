package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.Atendimento;
import br.pucminas.hospital.model.AtendimentoEmergencia;
import br.pucminas.hospital.model.Consulta;
import br.pucminas.hospital.model.ConsultaEspecializada;
import br.pucminas.hospital.model.ConsultaRotina;
import br.pucminas.hospital.model.PrioridadeEmergencia;
import br.pucminas.hospital.model.TipoConsulta;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalTime;

/** Campos que não se aplicam ao tipo do atendimento são omitidos do JSON. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConsultaResponse(
        Long id,
        TipoConsulta tipo,
        Long pacienteId,
        String pacienteNome,
        Long profissionalId,
        String profissionalNome,
        LocalDate data,
        LocalTime horario,
        String situacao,
        String motivo,
        String observacoesMedicas,
        String especialidade,
        String queixa,
        PrioridadeEmergencia prioridade) {

    public static ConsultaResponse de(Atendimento atendimento) {
        TipoConsulta tipo = TipoConsulta.EMERGENCIA;
        String situacao = null;
        String motivo = null;
        String observacoes = null;
        String especialidade = null;
        String queixa = null;
        PrioridadeEmergencia prioridade = null;

        if (atendimento instanceof Consulta consulta) {
            tipo = consulta.getTipo();
            situacao = consulta.getSituacao().name();
            observacoes = consulta.getObservacoesMedicas();
        }
        if (atendimento instanceof ConsultaRotina rotina) {
            motivo = rotina.getMotivo();
        } else if (atendimento instanceof ConsultaEspecializada especializada) {
            especialidade = especializada.getEspecialidade();
        } else if (atendimento instanceof AtendimentoEmergencia emergencia) {
            situacao = emergencia.getSituacao().name();
            queixa = emergencia.getQueixa();
            prioridade = emergencia.getPrioridade();
        }

        return new ConsultaResponse(
                atendimento.getId(),
                tipo,
                atendimento.getPaciente().getId(),
                atendimento.getPaciente().getNome(),
                atendimento.getProfissional().getId(),
                atendimento.getProfissional().getNome(),
                atendimento.getData(),
                atendimento.getHorario(),
                situacao,
                motivo,
                observacoes,
                especialidade,
                queixa,
                prioridade);
    }
}
