package br.pucminas.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ObservacoesRequest(
        @NotBlank(message = "As observações são obrigatórias.")
        @Size(max = 2000, message = "As observações devem ter no máximo 2000 caracteres.")
        String observacoesMedicas) {
}
