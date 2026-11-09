package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.ProcedimentoCirurgico;
import br.pucminas.hospital.model.StatusProcedimento;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ProcedimentoResponse(
        Long id,
        Long cirurgiaoId,
        String cirurgiaoNome,
        Long pacienteId,
        String pacienteNome,
        LocalDate data,
        LocalTime horario,
        String descricao,
        String tipoCirurgia,
        StatusProcedimento status,
        String observacoesPreOperatorias,
        String observacoesPosOperatorias,
        LocalDateTime inicioPrevisto,
        LocalDateTime terminoPrevisto,
        LocalDateTime inicioEfetivo,
        LocalDateTime terminoEfetivo) {

    public static ProcedimentoResponse de(ProcedimentoCirurgico procedimento) {
        return new ProcedimentoResponse(
                procedimento.getId(),
                procedimento.getProfissional().getId(),
                procedimento.getProfissional().getNome(),
                procedimento.getPaciente().getId(),
                procedimento.getPaciente().getNome(),
                procedimento.getData(),
                procedimento.getHorario(),
                procedimento.getDescricao(),
                procedimento.getTipoCirurgia(),
                procedimento.getStatus(),
                procedimento.getObservacoesPreOperatorias(),
                procedimento.getObservacoesPosOperatorias(),
                procedimento.getInicioPrevisto(),
                procedimento.getTerminoPrevisto(),
                procedimento.getInicioEfetivo(),
                procedimento.getTerminoEfetivo());
    }
}
