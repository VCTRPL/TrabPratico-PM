package br.pucminas.hospital.service;

import java.time.LocalDate;
import java.time.LocalTime;

/** Responde se um profissional está livre em um horário, consultando os atendimentos já marcados. */
public interface AgendaProfissional {

    boolean estaLivre(Long profissionalId, LocalDate data, LocalTime horario);
}
