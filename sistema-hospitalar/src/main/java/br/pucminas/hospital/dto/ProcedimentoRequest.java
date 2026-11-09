package br.pucminas.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** {@code data} e {@code horario} são o início previsto; {@code terminoPrevisto} é opcional. */
public record ProcedimentoRequest(
        @NotNull(message = "O paciente é obrigatório.") Long pacienteId,
        @NotNull(message = "A data prevista é obrigatória.") LocalDate data,
        @NotNull(message = "O horário previsto é obrigatório.") LocalTime horario,
        LocalDateTime terminoPrevisto,
        @NotBlank(message = "A descrição é obrigatória.") String descricao,
        @NotBlank(message = "O tipo de cirurgia é obrigatório.") String tipoCirurgia,
        String observacoesPreOperatorias) {
}
