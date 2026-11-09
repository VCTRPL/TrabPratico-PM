package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.Internacao;
import java.time.LocalDate;

public record InternacaoResponse(
        Long id,
        Long pacienteId,
        String pacienteNome,
        Long profissionalId,
        String profissionalNome,
        Long quartoId,
        int quartoNumero,
        LocalDate dataEntrada,
        LocalDate dataPrevistaAlta,
        LocalDate dataEfetivaAlta,
        String observacoes,
        boolean ativa) {

    public static InternacaoResponse de(Internacao internacao) {
        return new InternacaoResponse(
                internacao.getId(),
                internacao.getPaciente().getId(),
                internacao.getPaciente().getNome(),
                internacao.getProfissionalResponsavel().getId(),
                internacao.getProfissionalResponsavel().getNome(),
                internacao.getQuarto().getId(),
                internacao.getQuarto().getNumero(),
                internacao.getDataEntrada(),
                internacao.getDataPrevistaAlta(),
                internacao.getDataEfetivaAlta(),
                internacao.getObservacoes(),
                internacao.estaAtiva());
    }
}
