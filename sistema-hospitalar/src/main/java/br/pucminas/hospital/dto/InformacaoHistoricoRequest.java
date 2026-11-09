package br.pucminas.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InformacaoHistoricoRequest(
        @NotBlank(message = "A informação é obrigatória.")
        @Size(max = 1000, message = "A informação deve ter no máximo 1000 caracteres.")
        String informacao) {
}
