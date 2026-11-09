package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.TipoQuarto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record QuartoRequest(
        @NotNull(message = "O número do quarto é obrigatório.")
        @Min(value = 1, message = "O número do quarto deve ser positivo.")
        Integer numero,
        @NotNull(message = "O andar é obrigatório.") Integer andar,
        @NotNull(message = "O tipo do quarto é obrigatório.") TipoQuarto tipo,
        @NotNull(message = "A capacidade máxima é obrigatória.")
        @Min(value = 1, message = "A capacidade máxima deve ser de pelo menos 1 leito.")
        Integer capacidadeMaxima) {
}
