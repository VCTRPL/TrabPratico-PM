package br.pucminas.hospital.controller;

import br.pucminas.hospital.dto.AltaRequest;
import br.pucminas.hospital.dto.InternacaoAtualizacaoRequest;
import br.pucminas.hospital.dto.InternacaoRequest;
import br.pucminas.hospital.dto.InternacaoResponse;
import br.pucminas.hospital.service.InternacaoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internacoes")
public class InternacaoController {

    private final InternacaoService service;

    public InternacaoController(InternacaoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InternacaoResponse internar(@Valid @RequestBody InternacaoRequest dados) {
        return service.internar(dados);
    }

    @GetMapping
    public List<InternacaoResponse> listar(@RequestParam(required = false) Boolean ativas) {
        return service.listar(ativas);
    }

    @GetMapping("/{id}")
    public InternacaoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public InternacaoResponse atualizar(@PathVariable Long id, @RequestBody InternacaoAtualizacaoRequest dados) {
        return service.atualizar(id, dados);
    }

    @PostMapping("/{id}/alta")
    public InternacaoResponse darAlta(@PathVariable Long id, @RequestBody(required = false) AltaRequest dados) {
        return service.darAlta(id, dados == null ? null : dados.dataAlta());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
