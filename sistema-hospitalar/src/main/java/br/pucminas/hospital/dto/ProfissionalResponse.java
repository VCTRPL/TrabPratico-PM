package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.ProfissionalSaude;
import br.pucminas.hospital.model.TipoProfissional;

public record ProfissionalResponse(
        Long id,
        TipoProfissional tipo,
        String nome,
        String registroProfissional,
        String telefone,
        String email,
        String areaAtuacao) {

    public static ProfissionalResponse de(ProfissionalSaude profissional) {
        return new ProfissionalResponse(
                profissional.getId(),
                profissional.getTipo(),
                profissional.getNome(),
                profissional.getRegistroProfissional(),
                profissional.getTelefone(),
                profissional.getEmail(),
                profissional.getAreaAtuacao());
    }
}
