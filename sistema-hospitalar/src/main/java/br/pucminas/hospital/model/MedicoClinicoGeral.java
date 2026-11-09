package br.pucminas.hospital.model;

import jakarta.persistence.Entity;

@Entity
public class MedicoClinicoGeral extends ProfissionalSaude {

    private static final String AREA = "Clínica Geral";

    protected MedicoClinicoGeral() {
    }

    public MedicoClinicoGeral(String nome, String registroProfissional, String telefone, String email) {
        super(nome, registroProfissional, telefone, email);
    }

    public Encaminhamento encaminhar(Paciente paciente, String especialidadeDestino, String motivo) {
        return new Encaminhamento(this, paciente, especialidadeDestino, motivo);
    }

    @Override
    public TipoProfissional getTipo() {
        return TipoProfissional.CLINICO_GERAL;
    }

    @Override
    public String getAreaAtuacao() {
        return AREA;
    }

    /** A área do clínico geral é fixa, então o valor recebido é ignorado. */
    @Override
    public void definirAreaAtuacao(String area) {
    }
}
