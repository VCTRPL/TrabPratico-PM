package br.pucminas.hospital.service;

import br.pucminas.hospital.dto.ConsultaRequest;
import br.pucminas.hospital.dto.ConsultaResponse;
import br.pucminas.hospital.dto.ReagendamentoRequest;
import br.pucminas.hospital.dto.SituacaoRequest;
import br.pucminas.hospital.exception.ConflitoException;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.Atendimento;
import br.pucminas.hospital.model.AtendimentoEmergencia;
import br.pucminas.hospital.model.Consulta;
import br.pucminas.hospital.model.ConsultaEspecializada;
import br.pucminas.hospital.model.ConsultaRotina;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.model.ProfissionalSaude;
import br.pucminas.hospital.model.SituacaoConsulta;
import br.pucminas.hospital.model.SituacaoEmergencia;
import br.pucminas.hospital.model.TipoConsulta;
import br.pucminas.hospital.repository.AtendimentoRepository;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConsultaService {

    private final AtendimentoRepository atendimentos;
    private final PacienteRepository pacientes;
    private final ProfissionalSaudeRepository profissionais;
    private final AgendaProfissional agenda;

    public ConsultaService(
            AtendimentoRepository atendimentos,
            PacienteRepository pacientes,
            ProfissionalSaudeRepository profissionais,
            AgendaProfissional agenda) {
        this.atendimentos = atendimentos;
        this.pacientes = pacientes;
        this.profissionais = profissionais;
        this.agenda = agenda;
    }

    /**
     * RN2: exige paciente e profissional existentes. As regras de cada tipo são checadas por
     * {@link Atendimento#validar()} na criação; depois, RN3 garante que o profissional está livre no horário.
     */
    public ConsultaResponse criar(ConsultaRequest dados) {
        Paciente paciente = pacientes.findById(dados.pacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Paciente " + dados.pacienteId() + " não encontrado."));
        ProfissionalSaude profissional = profissionais.findById(dados.profissionalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Profissional " + dados.profissionalId() + " não encontrado."));

        Atendimento atendimento = instanciar(dados, paciente, profissional);

        exigirAgendaLivre(profissional.getId(), atendimento.getData(), atendimento.getHorario());
        return ConsultaResponse.de(atendimentos.save(atendimento));
    }

    /** Novo agendamento: muda data e horário e devolve a consulta para AGENDADA (inclusive se estava cancelada). */
    public ConsultaResponse reagendar(Long id, ReagendamentoRequest dados) {
        if (!(obter(id) instanceof Consulta consulta)) {
            throw new RegraNegocioException("Apenas consultas podem ser reagendadas.");
        }
        exigirAgendaLivre(consulta.getProfissional().getId(), dados.data(), dados.horario());
        consulta.reagendar(dados.data(), dados.horario());
        return ConsultaResponse.de(consulta);
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponse> listar(
            TipoConsulta tipo, String situacao, Long pacienteId, Long profissionalId, LocalDate data) {
        String situacaoBuscada = situacao == null ? null : situacao.trim().toUpperCase(Locale.ROOT);
        return atendimentos.listarConsultasEEmergencias().stream()
                .map(ConsultaResponse::de)
                .filter(consulta -> tipo == null || consulta.tipo() == tipo)
                .filter(consulta -> situacaoBuscada == null || situacaoBuscada.equals(consulta.situacao()))
                .filter(consulta -> pacienteId == null || consulta.pacienteId().equals(pacienteId))
                .filter(consulta -> profissionalId == null || consulta.profissionalId().equals(profissionalId))
                .filter(consulta -> data == null || consulta.data().equals(data))
                .toList();
    }

    @Transactional(readOnly = true)
    public ConsultaResponse buscar(Long id) {
        return ConsultaResponse.de(obter(id));
    }

    public ConsultaResponse alterarSituacao(Long id, SituacaoRequest dados) {
        Atendimento atendimento = obter(id);
        if (atendimento instanceof Consulta consulta) {
            consulta.alterarSituacao(SituacaoConsulta.de(dados.situacao()));
            if (dados.observacoesMedicas() != null && !dados.observacoesMedicas().isBlank()) {
                consulta.registrarObservacoes(dados.observacoesMedicas());
            }
        } else if (atendimento instanceof AtendimentoEmergencia emergencia) {
            emergencia.alterarSituacao(SituacaoEmergencia.de(dados.situacao()));
        }
        return ConsultaResponse.de(atendimento);
    }

    public ConsultaResponse registrarObservacoes(Long id, String observacoes) {
        Atendimento atendimento = obter(id);
        registrarObservacoes(atendimento, observacoes);
        return ConsultaResponse.de(atendimento);
    }

    private void registrarObservacoes(Atendimento atendimento, String observacoes) {
        if (!(atendimento instanceof Consulta consulta)) {
            throw new RegraNegocioException("Observações médicas só são registradas em consultas.");
        }
        consulta.registrarObservacoes(observacoes);
    }

    private void exigirAgendaLivre(Long profissionalId, LocalDate data, LocalTime horario) {
        if (!agenda.estaLivre(profissionalId, data, horario)) {
            throw new ConflitoException("O profissional já possui um atendimento neste horário.");
        }
    }

    private Atendimento instanciar(ConsultaRequest d, Paciente paciente, ProfissionalSaude profissional) {
        return switch (d.tipo()) {
            case ROTINA -> new ConsultaRotina(paciente, profissional, d.data(), d.horario(), d.motivo());
            case ESPECIALIZADA -> new ConsultaEspecializada(
                    paciente, profissional, d.data(), d.horario(), d.especialidade());
            case EMERGENCIA -> {
                LocalDateTime agora = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
                LocalDate data = d.data() != null ? d.data() : agora.toLocalDate();
                LocalTime horario = d.horario() != null ? d.horario() : agora.toLocalTime();
                yield new AtendimentoEmergencia(paciente, profissional, data, horario, d.queixa(), d.prioridade());
            }
        };
    }

    /** Procedimentos cirúrgicos também são atendimentos, mas têm endpoint próprio e não aparecem aqui. */
    private Atendimento obter(Long id) {
        return atendimentos.findById(id)
                .filter(a -> a instanceof Consulta || a instanceof AtendimentoEmergencia)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta " + id + " não encontrada."));
    }
}
