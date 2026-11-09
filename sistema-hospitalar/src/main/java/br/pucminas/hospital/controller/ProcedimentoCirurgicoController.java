package br.pucminas.hospital.controller;

import br.pucminas.hospital.dto.ConclusaoProcedimentoRequest;
import br.pucminas.hospital.dto.ProcedimentoRequest;
import br.pucminas.hospital.dto.ProcedimentoResponse;
import br.pucminas.hospital.model.StatusProcedimento;
import br.pucminas.hospital.service.ProcedimentoCirurgicoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Os procedimentos são registrados por cirurgião, em /profissionais/{id}/procedimentos. */
@RestController
public class ProcedimentoCirurgicoController {

    private final ProcedimentoCirurgicoService service;

    public ProcedimentoCirurgicoController(ProcedimentoCirurgicoService service) {
        this.service = service;
    }

    @PostMapping("/profissionais/{id}/procedimentos")
    @ResponseStatus(HttpStatus.CREATED)
    public ProcedimentoResponse agendar(@PathVariable Long id, @Valid @RequestBody ProcedimentoRequest dados) {
        return service.agendar(id, dados);
    }

    @GetMapping("/profissionais/{id}/procedimentos")
    public List<ProcedimentoResponse> listarDoCirurgiao(
            @PathVariable Long id, @RequestParam(required = false) StatusProcedimento status) {
        return service.listarDoCirurgiao(id, status);
    }

    @GetMapping("/procedimentos/{id}")
    public ProcedimentoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping("/procedimentos/{id}/iniciar")
    public ProcedimentoResponse iniciar(@PathVariable Long id) {
        return service.iniciar(id);
    }

    @PostMapping("/procedimentos/{id}/concluir")
    public ProcedimentoResponse concluir(
            @PathVariable Long id, @RequestBody(required = false) ConclusaoProcedimentoRequest dados) {
        return service.concluir(id, dados == null ? null : dados.observacoesPosOperatorias());
    }

    @PostMapping("/procedimentos/{id}/cancelar")
    public ProcedimentoResponse cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }
}
