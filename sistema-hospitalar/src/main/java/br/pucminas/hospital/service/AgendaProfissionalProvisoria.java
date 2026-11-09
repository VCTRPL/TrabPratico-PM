package br.pucminas.hospital.service;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Component;

/**
 * TODO (frente de consultas): apagar esta classe e implementar {@link AgendaProfissional} consultando os
 * atendimentos. Enquanto ela existir, todo profissional é considerado livre em qualquer horário.
 */
@Component
class AgendaProfissionalProvisoria implements AgendaProfissional {

    @Override
    public boolean estaLivre(Long profissionalId, LocalDate data, LocalTime horario) {
        return true;
    }
}
