package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.Paciente;
import java.util.List;

public record HistoricoMedicoResponse(Long pacienteId, String pacienteNome, List<String> informacoesRelevantes) {

    public static HistoricoMedicoResponse de(Paciente paciente) {
        return new HistoricoMedicoResponse(
                paciente.getId(), paciente.getNome(), paciente.getHistorico().getInformacoesRelevantes());
    }
}
