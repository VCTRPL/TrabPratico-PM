package br.pucminas.hospital.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Cirurgiao extends ProfissionalSaude {

    @Column(nullable = false)
    private String areaCirurgica;

    protected Cirurgiao() {
    }

    public Cirurgiao(String nome, String registroProfissional, String telefone, String email, String areaCirurgica) {
        super(nome, registroProfissional, telefone, email);
        definirAreaAtuacao(areaCirurgica);
    }

    @Override
    public TipoProfissional getTipo() {
        return TipoProfissional.CIRURGIAO;
    }

    @Override
    public String getAreaAtuacao() {
        return areaCirurgica;
    }

    @Override
    public final void definirAreaAtuacao(String area) {
        this.areaCirurgica = exigirArea(area, "O cirurgião deve possuir uma área cirúrgica.");
    }
}
