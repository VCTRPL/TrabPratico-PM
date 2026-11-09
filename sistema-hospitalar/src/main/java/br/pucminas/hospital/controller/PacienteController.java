package br.pucminas.hospital.controller;

import br.pucminas.hospital.dto.HistoricoMedicoResponse;
import br.pucminas.hospital.dto.InformacaoHistoricoRequest;
import br.pucminas.hospital.dto.PacienteRequest;
import br.pucminas.hospital.dto.PacienteResponse;
import br.pucminas.hospital.service.PacienteService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteResponse criar(@Valid @RequestBody PacienteRequest dados) {
        return service.criar(dados);
    }

    @GetMapping
    public List<PacienteResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public PacienteResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public PacienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequest dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @GetMapping("/{id}/historico")
    public HistoricoMedicoResponse buscarHistorico(@PathVariable Long id) {
        return service.buscarHistorico(id);
    }

    @PostMapping("/{id}/historico")
    @ResponseStatus(HttpStatus.CREATED)
    public HistoricoMedicoResponse adicionarAoHistorico(
            @PathVariable Long id, @Valid @RequestBody InformacaoHistoricoRequest dados) {
        return service.adicionarAoHistorico(id, dados.informacao());
    }
}
