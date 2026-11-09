package br.pucminas.hospital.service;

import br.pucminas.hospital.dto.HistoricoMedicoResponse;
import br.pucminas.hospital.dto.PacienteRequest;
import br.pucminas.hospital.dto.PacienteResponse;
import br.pucminas.hospital.exception.ConflitoException;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.model.Paciente;
import br.pucminas.hospital.repository.PacienteRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PacienteService {

    private final PacienteRepository pacientes;

    public PacienteService(PacienteRepository pacientes) {
        this.pacientes = pacientes;
    }

    public PacienteResponse criar(PacienteRequest dados) {
        if (pacientes.existsByCpf(dados.cpf())) {
            throw new ConflitoException("Já existe um paciente com este CPF.");
        }
        Paciente paciente = new Paciente(
                dados.nome(), dados.cpf(), dados.dataNascimento(), dados.telefone(), dados.endereco(), dados.email());
        return PacienteResponse.de(pacientes.save(paciente));
    }

    @Transactional(readOnly = true)
    public List<PacienteResponse> listar() {
        return pacientes.findAll().stream().map(PacienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PacienteResponse buscar(Long id) {
        return PacienteResponse.de(obter(id));
    }

    public PacienteResponse atualizar(Long id, PacienteRequest dados) {
        Paciente paciente = obter(id);
        if (pacientes.existsByCpfAndIdNot(dados.cpf(), id)) {
            throw new ConflitoException("Já existe um paciente com este CPF.");
        }
        paciente.atualizarDados(
                dados.nome(), dados.cpf(), dados.dataNascimento(), dados.telefone(), dados.endereco(), dados.email());
        return PacienteResponse.de(paciente);
    }

    public void excluir(Long id) {
        pacientes.delete(obter(id));
    }

    @Transactional(readOnly = true)
    public HistoricoMedicoResponse buscarHistorico(Long id) {
        return HistoricoMedicoResponse.de(obter(id));
    }

    public HistoricoMedicoResponse adicionarAoHistorico(Long id, String informacao) {
        Paciente paciente = obter(id);
        paciente.getHistorico().adicionarInformacao(informacao);
        return HistoricoMedicoResponse.de(paciente);
    }

    private Paciente obter(Long id) {
        return pacientes.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente " + id + " não encontrado."));
    }
}
