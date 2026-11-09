package br.pucminas.hospital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record PacienteRequest(
        @NotBlank(message = "O nome é obrigatório.") String nome,
        @NotBlank(message = "O CPF é obrigatório.")
        @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "O CPF deve estar no formato 000.000.000-00.")
        String cpf,
        @NotNull(message = "A data de nascimento é obrigatória.")
        @Past(message = "A data de nascimento deve estar no passado.")
        LocalDate dataNascimento,
        @NotBlank(message = "O telefone é obrigatório.") String telefone,
        @NotBlank(message = "O endereço é obrigatório.") String endereco,
        @NotBlank(message = "O e-mail é obrigatório.") @Email(message = "E-mail inválido.") String email) {
}
