package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.Quarto;
import br.pucminas.hospital.model.SituacaoQuarto;
import br.pucminas.hospital.model.TipoQuarto;

public record QuartoResponse(
        Long id,
        int numero,
        int andar,
        TipoQuarto tipo,
        int capacidadeMaxima,
        int ocupacaoAtual,
        int vagasLivres,
        SituacaoQuarto situacao) {

    public static QuartoResponse de(Quarto quarto) {
        return new QuartoResponse(
                quarto.getId(),
                quarto.getNumero(),
                quarto.getAndar(),
                quarto.getTipo(),
                quarto.getCapacidadeMaxima(),
                quarto.ocupacaoAtual(),
                quarto.getCapacidadeMaxima() - quarto.ocupacaoAtual(),
                quarto.getSituacao());
    }
}
