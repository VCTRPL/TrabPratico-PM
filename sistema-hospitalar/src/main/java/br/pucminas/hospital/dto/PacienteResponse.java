package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.Paciente;
import java.time.LocalDate;

public record PacienteResponse(
        Long id, String nome, String cpf, LocalDate dataNascimento, String telefone, String endereco, String email) {

    public static PacienteResponse de(Paciente paciente) {
        return new PacienteResponse(
                paciente.getId(),
                paciente.getNome(),
                paciente.getCpf(),
                paciente.getDataNascimento(),
                paciente.getTelefone(),
                paciente.getEndereco(),
                paciente.getEmail());
    }
}
