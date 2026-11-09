package br.pucminas.hospital.repository;

import br.pucminas.hospital.model.Atendimento;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Consulta polimórfica: devolve consultas, emergências e procedimentos cirúrgicos juntos. */
public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {

    List<Atendimento> findByProfissionalIdAndDataAndHorario(Long profissionalId, LocalDate data, LocalTime horario);

    /** Apenas os atendimentos tratados pelo endpoint /consultas, em ordem cronológica. */
    @Query("""
            select a from Atendimento a
            where type(a) in (ConsultaRotina, ConsultaEspecializada, AtendimentoEmergencia)
            order by a.data, a.horario
            """)
    List<Atendimento> listarConsultasEEmergencias();
}
