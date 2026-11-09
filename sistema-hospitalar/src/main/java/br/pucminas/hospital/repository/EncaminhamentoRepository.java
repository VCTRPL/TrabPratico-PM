package br.pucminas.hospital.repository;

import br.pucminas.hospital.model.Encaminhamento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncaminhamentoRepository extends JpaRepository<Encaminhamento, Long> {

    List<Encaminhamento> findByClinicoId(Long clinicoId);
}
