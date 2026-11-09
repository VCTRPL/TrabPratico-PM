package br.pucminas.hospital.service;

import br.pucminas.hospital.dto.EncaminhamentoRequest;
import br.pucminas.hospital.dto.EncaminhamentoResponse;
import br.pucminas.hospital.dto.ProfissionalRequest;
import br.pucminas.hospital.dto.ProfissionalResponse;
import br.pucminas.hospital.exception.ConflitoException;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.Cirurgiao;
import br.pucminas.hospital.model.MedicoClinicoGeral;
import br.pucminas.hospital.model.MedicoEspecialista;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.model.ProfissionalSaude;
import br.pucminas.hospital.repository.EncaminhamentoRepository;
import br.pucminas.hospital.repository.PacienteRepository;
import br.pucminas.hospital.repository.ProfissionalSaudeRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProfissionalService {

    private final ProfissionalSaudeRepository profissionais;
    private final PacienteRepository pacientes;
    private final EncaminhamentoRepository encaminhamentos;
    private final AgendaProfissional agenda;

    public ProfissionalService(
            ProfissionalSaudeRepository profissionais,
            PacienteRepository pacientes,
            EncaminhamentoRepository encaminhamentos,
            AgendaProfissional agenda) {
        this.profissionais = profissionais;
        this.pacientes = pacientes;
        this.encaminhamentos = encaminhamentos;
        this.agenda = agenda;
    }

    public ProfissionalResponse criar(ProfissionalRequest dados) {
        if (profissionais.existsByRegistroProfissional(dados.registroProfissional())) {
            throw new ConflitoException("Já existe um profissional com este registro.");
        }
        return ProfissionalResponse.de(profissionais.save(instanciar(dados)));
    }

    /** Filtra por área e, quando data e horário são informados, mantém só quem está livre nesse momento. */
    @Transactional(readOnly = true)
    public List<ProfissionalResponse> listar(String area, LocalDate data, LocalTime horario) {
        if ((data == null) != (horario == null)) {
            throw new RegraNegocioException("Informe data e horário juntos para consultar a disponibilidade.");
        }
        return profissionais.findAll().stream()
                .filter(profissional -> area == null || profissional.atuaNaArea(area))
                .filter(profissional -> data == null || agenda.estaLivre(profissional.getId(), data, horario))
                .map(ProfissionalResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfissionalResponse buscar(Long id) {
        return ProfissionalResponse.de(obter(id));
    }

    public ProfissionalResponse atualizar(Long id, ProfissionalRequest dados) {
        ProfissionalSaude profissional = obter(id);
        if (profissional.getTipo() != dados.tipo()) {
            throw new RegraNegocioException("A categoria de um profissional não pode ser alterada.");
        }
        if (profissionais.existsByRegistroProfissionalAndIdNot(dados.registroProfissional(), id)) {
            throw new ConflitoException("Já existe um profissional com este registro.");
        }
        profissional.atualizarDados(dados.nome(), dados.registroProfissional(), dados.telefone(), dados.email());
        profissional.definirAreaAtuacao(dados.areaAtuacao());
        return ProfissionalResponse.de(profissional);
    }

    public void excluir(Long id) {
        profissionais.delete(obter(id));
    }

    public EncaminhamentoResponse encaminhar(Long clinicoId, EncaminhamentoRequest dados) {
        if (!(obter(clinicoId) instanceof MedicoClinicoGeral clinico)) {
            throw new RegraNegocioException("Apenas médicos clínicos gerais podem encaminhar pacientes.");
        }
        Paciente paciente = pacientes.findById(dados.pacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Paciente " + dados.pacienteId() + " não encontrado."));
        return EncaminhamentoResponse.de(
                encaminhamentos.save(clinico.encaminhar(paciente, dados.especialidadeDestino(), dados.motivo())));
    }

    @Transactional(readOnly = true)
    public List<EncaminhamentoResponse> listarEncaminhamentos(Long clinicoId) {
        obter(clinicoId);
        return encaminhamentos.findByClinicoId(clinicoId).stream().map(EncaminhamentoResponse::de).toList();
    }

    private ProfissionalSaude instanciar(ProfissionalRequest d) {
        return switch (d.tipo()) {
            case CLINICO_GERAL -> new MedicoClinicoGeral(d.nome(), d.registroProfissional(), d.telefone(), d.email());
            case ESPECIALISTA -> new MedicoEspecialista(
                    d.nome(), d.registroProfissional(), d.telefone(), d.email(), d.areaAtuacao());
            case CIRURGIAO -> new Cirurgiao(
                    d.nome(), d.registroProfissional(), d.telefone(), d.email(), d.areaAtuacao());
        };
    }

    private ProfissionalSaude obter(Long id) {
        return profissionais.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional " + id + " não encontrado."));
    }
}
