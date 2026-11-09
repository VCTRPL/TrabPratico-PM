package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.Encaminhamento;
import java.time.LocalDate;

public record EncaminhamentoResponse(
        Long id,
        Long clinicoId,
        String clinicoNome,
        Long pacienteId,
        String pacienteNome,
        String especialidadeDestino,
        String motivo,
        LocalDate dataEncaminhamento) {

    public static EncaminhamentoResponse de(Encaminhamento encaminhamento) {
        return new EncaminhamentoResponse(
                encaminhamento.getId(),
                encaminhamento.getClinico().getId(),
                encaminhamento.getClinico().getNome(),
                encaminhamento.getPaciente().getId(),
                encaminhamento.getPaciente().getNome(),
                encaminhamento.getEspecialidadeDestino(),
                encaminhamento.getMotivo(),
                encaminhamento.getDataEncaminhamento());
    }
}
