package br.pucminas.hospital.service;

import br.pucminas.hospital.dto.ProcedimentoRequest;
import br.pucminas.hospital.dto.ProcedimentoResponse;
import br.pucminas.hospital.exception.ConflitoException;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.Cirurgiao;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.model.ProcedimentoCirurgico;
import br.pucminas.hospital.model.StatusProcedimento;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProcedimentoCirurgicoRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProcedimentoCirurgicoService {

    private final ProcedimentoCirurgicoRepository procedimentos;
    private final ProfissionalSaudeRepository profissionais;
    private final PacienteRepository pacientes;
    private final AgendaProfissional agenda;

    public ProcedimentoCirurgicoService(
            ProcedimentoCirurgicoRepository procedimentos,
            ProfissionalSaudeRepository profissionais,
            PacienteRepository pacientes,
            AgendaProfissional agenda) {
        this.procedimentos = procedimentos;
        this.profissionais = profissionais;
        this.pacientes = pacientes;
        this.agenda = agenda;
    }

    public ProcedimentoResponse agendar(Long cirurgiaoId, ProcedimentoRequest dados) {
        Cirurgiao cirurgiao = obterCirurgiao(cirurgiaoId);
        Paciente paciente = pacientes.findById(dados.pacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Paciente " + dados.pacienteId() + " não encontrado."));
        if (!agenda.estaLivre(cirurgiaoId, dados.data(), dados.horario())) {
            throw new ConflitoException("O cirurgião já possui um atendimento neste horário.");
        }
        ProcedimentoCirurgico procedimento = new ProcedimentoCirurgico(
                cirurgiao,
                paciente,
                dados.data(),
                dados.horario(),
                dados.terminoPrevisto(),
                dados.descricao(),
                dados.tipoCirurgia(),
                dados.observacoesPreOperatorias());
        return ProcedimentoResponse.de(procedimentos.save(procedimento));
    }

    /** Procedimentos do cirurgião em ordem cronológica, opcionalmente filtrados por status. */
    @Transactional(readOnly = true)
    public List<ProcedimentoResponse> listarDoCirurgiao(Long cirurgiaoId, StatusProcedimento status) {
        obterCirurgiao(cirurgiaoId);
        return procedimentos.findByProfissionalIdOrderByDataAscHorarioAsc(cirurgiaoId).stream()
                .filter(procedimento -> status == null || procedimento.getStatus() == status)
                .map(ProcedimentoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProcedimentoResponse buscar(Long id) {
        return ProcedimentoResponse.de(obter(id));
    }

    public ProcedimentoResponse iniciar(Long id) {
        ProcedimentoCirurgico procedimento = obter(id);
        procedimento.iniciar(LocalDateTime.now());
        return ProcedimentoResponse.de(procedimento);
    }

    public ProcedimentoResponse concluir(Long id, String observacoesPosOperatorias) {
        ProcedimentoCirurgico procedimento = obter(id);
        procedimento.concluir(LocalDateTime.now(), observacoesPosOperatorias);
        return ProcedimentoResponse.de(procedimento);
    }

    public ProcedimentoResponse cancelar(Long id) {
        ProcedimentoCirurgico procedimento = obter(id);
        procedimento.cancelar();
        return ProcedimentoResponse.de(procedimento);
    }

    private Cirurgiao obterCirurgiao(Long id) {
        var profissional = profissionais.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional " + id + " não encontrado."));
        if (!(profissional instanceof Cirurgiao cirurgiao)) {
            throw new RegraNegocioException("Apenas cirurgiões possuem procedimentos cirúrgicos.");
        }
        return cirurgiao;
    }

    private ProcedimentoCirurgico obter(Long id) {
        return procedimentos.findById(id).orElseThrow(
                () -> new RecursoNaoEncontradoException("Procedimento cirúrgico " + id + " não encontrado."));
    }
}
