package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.PrioridadeEmergencia;
import br.pucminas.hospital.model.TipoConsulta;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Dados para registrar um atendimento em /consultas. Os campos usados dependem do {@code tipo}:
 * <ul>
 *   <li>ROTINA: {@code motivo};</li>
 *   <li>ESPECIALIZADA: {@code especialidade};</li>
 *   <li>EMERGENCIA: {@code queixa} e {@code prioridade}. Sem data e horário, usa o momento atual.</li>
 * </ul>
 */
public record ConsultaRequest(
        @NotNull(message = "O tipo da consulta é obrigatório.") TipoConsulta tipo,
        @NotNull(message = "O paciente é obrigatório.") Long pacienteId,
        @NotNull(message = "O profissional responsável é obrigatório.") Long profissionalId,
        LocalDate data,
        LocalTime horario,
        String motivo,
        String especialidade,
        String queixa,
        PrioridadeEmergencia prioridade) {
}
