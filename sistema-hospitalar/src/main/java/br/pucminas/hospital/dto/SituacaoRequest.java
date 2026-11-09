package br.pucminas.hospital.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Nova situação de uma consulta (AGENDADA, CONFIRMADA, EM_ATENDIMENTO, CONCLUIDA, CANCELADA, NAO_COMPARECEU)
 * ou de uma emergência (AGUARDANDO, INICIADO, CONCLUIDO). Nas consultas, {@code observacoesMedicas} pode ser
 * enviado junto da conclusão.
 */
public record SituacaoRequest(
        @NotBlank(message = "A situação é obrigatória.") String situacao,
        String observacoesMedicas) {
}
