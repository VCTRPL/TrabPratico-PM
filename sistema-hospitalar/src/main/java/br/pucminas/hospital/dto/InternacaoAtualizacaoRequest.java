package br.pucminas.hospital.dto;

import java.time.LocalDate;

/** Paciente, profissional e quarto não mudam depois de registrada a internação; só a previsão e as observações. */
public record InternacaoAtualizacaoRequest(LocalDate dataPrevistaAlta, String observacoes) {
}
