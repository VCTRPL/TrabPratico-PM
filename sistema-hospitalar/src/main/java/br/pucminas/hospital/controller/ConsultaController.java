package br.pucminas.hospital.controller;

import br.pucminas.hospital.dto.ConsultaRequest;
import br.pucminas.hospital.dto.ConsultaResponse;
import br.pucminas.hospital.dto.ObservacoesRequest;
import br.pucminas.hospital.dto.ReagendamentoRequest;
import br.pucminas.hospital.dto.SituacaoRequest;
import br.pucminas.hospital.model.TipoConsulta;
import br.pucminas.hospital.service.ConsultaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultaResponse criar(@Valid @RequestBody ConsultaRequest dados) {
        return service.criar(dados);
    }

    @GetMapping
    public List<ConsultaResponse> listar(
            @RequestParam(required = false) TipoConsulta tipo,
            @RequestParam(required = false) String situacao,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) Long profissionalId,
            @RequestParam(required = false) LocalDate data) {
        return service.listar(tipo, situacao, pacienteId, profissionalId, data);
    }

    @GetMapping("/{id}")
    public ConsultaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PatchMapping("/{id}/situacao")
    public ConsultaResponse alterarSituacao(@PathVariable Long id, @Valid @RequestBody SituacaoRequest dados) {
        return service.alterarSituacao(id, dados);
    }

    @PatchMapping("/{id}/reagendamento")
    public ConsultaResponse reagendar(@PathVariable Long id, @Valid @RequestBody ReagendamentoRequest dados) {
        return service.reagendar(id, dados);
    }

    @PatchMapping("/{id}/observacoes")
    public ConsultaResponse registrarObservacoes(@PathVariable Long id, @Valid @RequestBody ObservacoesRequest dados) {
        return service.registrarObservacoes(id, dados.observacoesMedicas());
    }
}
