package br.pucminas.hospital.service;

import br.pucminas.hospital.dto.InternacaoAtualizacaoRequest;
import br.pucminas.hospital.dto.InternacaoRequest;
import br.pucminas.hospital.dto.InternacaoResponse;
import br.pucminas.hospital.exception.ConflitoException;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.model.Internacao;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.model.ProfissionalSaude;
import br.pucminas.hospital.model.Quarto;
import br.pucminas.hospital.repository.InternacaoRepository;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;
import br.pucminas.hospital.repository.QuartoRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InternacaoService {

    private final InternacaoRepository internacoes;
    private final PacienteRepository pacientes;
    private final ProfissionalSaudeRepository profissionais;
    private final QuartoRepository quartos;

    public InternacaoService(
            InternacaoRepository internacoes,
            PacienteRepository pacientes,
            ProfissionalSaudeRepository profissionais,
            QuartoRepository quartos) {
        this.internacoes = internacoes;
        this.pacientes = pacientes;
        this.profissionais = profissionais;
        this.quartos = quartos;
    }

    /** RN4 e RN5: exige paciente e quarto com vaga; a própria internação ocupa a vaga do quarto. */
    public InternacaoResponse internar(InternacaoRequest dados) {
        Paciente paciente = pacientes.findById(dados.pacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Paciente " + dados.pacienteId() + " não encontrado."));
        ProfissionalSaude profissional = profissionais.findById(dados.profissionalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Profissional " + dados.profissionalId() + " não encontrado."));
        Quarto quarto = quartos.findById(dados.quartoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quarto " + dados.quartoId() + " não encontrado."));
        if (internacoes.existsByPacienteIdAndDataEfetivaAltaIsNull(paciente.getId())) {
            throw new ConflitoException("O paciente já possui uma internação ativa.");
        }
        Internacao internacao = new Internacao(
                paciente, profissional, quarto, dados.dataEntrada(), dados.dataPrevistaAlta(), dados.observacoes());
        return InternacaoResponse.de(internacoes.save(internacao));
    }

    /** Com {@code ativas=true}, mantém só as internações sem alta. */
    @Transactional(readOnly = true)
    public List<InternacaoResponse> listar(Boolean ativas) {
        return internacoes.findAll().stream()
                .filter(internacao -> !Boolean.TRUE.equals(ativas) || internacao.estaAtiva())
                .map(InternacaoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public InternacaoResponse buscar(Long id) {
        return InternacaoResponse.de(obter(id));
    }

    public InternacaoResponse atualizar(Long id, InternacaoAtualizacaoRequest dados) {
        Internacao internacao = obter(id);
        internacao.atualizarPrevisao(dados.dataPrevistaAlta(), dados.observacoes());
        return InternacaoResponse.de(internacao);
    }

    public InternacaoResponse darAlta(Long id, LocalDate dataAlta) {
        Internacao internacao = obter(id);
        internacao.darAlta(dataAlta == null ? LocalDate.now() : dataAlta);
        return InternacaoResponse.de(internacao);
    }

    public void excluir(Long id) {
        Internacao internacao = obter(id);
        internacao.liberarQuarto();
        internacoes.delete(internacao);
    }

    private Internacao obter(Long id) {
        return internacoes.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Internação " + id + " não encontrada."));
    }
}
