package br.pucminas.hospital.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class MedicoEspecialista extends ProfissionalSaude {

    @Column(nullable = false)
    private String especialidade;

    protected MedicoEspecialista() {
    }

    public MedicoEspecialista(
            String nome, String registroProfissional, String telefone, String email, String especialidade) {
        super(nome, registroProfissional, telefone, email);
        definirAreaAtuacao(especialidade);
    }

    @Override
    public TipoProfissional getTipo() {
        return TipoProfissional.ESPECIALISTA;
    }

    @Override
    public String getAreaAtuacao() {
        return especialidade;
    }

    @Override
    public final void definirAreaAtuacao(String area) {
        this.especialidade = exigirArea(area, "O médico especialista deve possuir uma especialidade.");
    }
}
