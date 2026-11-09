package br.pucminas.hospital.repository;

import br.pucminas.hospital.model.ProfissionalSaude;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfissionalSaudeRepository extends JpaRepository<ProfissionalSaude, Long> {

    boolean existsByRegistroProfissional(String registroProfissional);

    boolean existsByRegistroProfissionalAndIdNot(String registroProfissional, Long id);
}
