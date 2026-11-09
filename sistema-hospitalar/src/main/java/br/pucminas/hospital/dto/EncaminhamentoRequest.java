package br.pucminas.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EncaminhamentoRequest(
        @NotNull(message = "O paciente é obrigatório.") Long pacienteId,
        @NotBlank(message = "A especialidade de destino é obrigatória.") String especialidadeDestino,
        @NotBlank(message = "O motivo é obrigatório.")
        @Size(max = 1000, message = "O motivo deve ter no máximo 1000 caracteres.")
        String motivo) {
}
