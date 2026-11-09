package br.pucminas.hospital.dto;

import java.time.LocalDate;

/** Quando {@code dataAlta} não é informada, a alta é registrada com a data de hoje. */
public record AltaRequest(LocalDate dataAlta) {
}
