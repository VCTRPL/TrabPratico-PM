package br.pucminas.hospital.repository;

import br.pucminas.hospital.model.Internacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InternacaoRepository extends JpaRepository<Internacao, Long> {

    boolean existsByPacienteIdAndDataEfetivaAltaIsNull(Long pacienteId);

    boolean existsByQuartoId(Long quartoId);
}
