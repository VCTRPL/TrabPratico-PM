package br.pucminas.hospital.service;

import br.pucminas.hospital.model.Atendimento;
import br.pucminas.hospital.repository.AtendimentoRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * RN3: o profissional não pode ter dois atendimentos no mesmo horário. Considera todos os tipos de
 * {@link Atendimento} (consultas, emergências e cirurgias), ignorando os cancelados, que liberam a agenda.
 */
@Component
class AgendaProfissionalPorAtendimentos implements AgendaProfissional {

    private final AtendimentoRepository atendimentos;

    AgendaProfissionalPorAtendimentos(AtendimentoRepository atendimentos) {
        this.atendimentos = atendimentos;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaLivre(Long profissionalId, LocalDate data, LocalTime horario) {
        return atendimentos.findByProfissionalIdAndDataAndHorario(profissionalId, data, horario).stream()
                .noneMatch(Atendimento::ocupaAgenda);
    }
}
