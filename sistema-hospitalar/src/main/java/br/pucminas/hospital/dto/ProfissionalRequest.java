package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.TipoProfissional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** {@code areaAtuacao} é a especialidade ou a área cirúrgica, e é ignorada para o clínico geral. */
public record ProfissionalRequest(
        @NotNull(message = "A categoria do profissional é obrigatória.") TipoProfissional tipo,
        @NotBlank(message = "O nome é obrigatório.") String nome,
        @NotBlank(message = "O registro profissional é obrigatório.") String registroProfissional,
        @NotBlank(message = "O telefone é obrigatório.") String telefone,
        @NotBlank(message = "O e-mail é obrigatório.") @Email(message = "E-mail inválido.") String email,
        String areaAtuacao) {
}
