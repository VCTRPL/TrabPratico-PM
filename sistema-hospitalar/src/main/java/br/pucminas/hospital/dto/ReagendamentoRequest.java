package br.pucminas.hospital.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReagendamentoRequest(
        @NotNull(message = "A nova data é obrigatória.") LocalDate data,
        @NotNull(message = "O novo horário é obrigatório.") LocalTime horario) {
}
