package br.pucminas.hospital.repository;

import br.pucminas.hospital.model.ProcedimentoCirurgico;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcedimentoCirurgicoRepository extends JpaRepository<ProcedimentoCirurgico, Long> {

    List<ProcedimentoCirurgico> findByProfissionalIdOrderByDataAscHorarioAsc(Long cirurgiaoId);
}
