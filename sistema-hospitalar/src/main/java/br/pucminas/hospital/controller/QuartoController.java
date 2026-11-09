package br.pucminas.hospital.controller;

import br.pucminas.hospital.dto.QuartoRequest;
import br.pucminas.hospital.dto.QuartoResponse;
import br.pucminas.hospital.model.TipoQuarto;
import br.pucminas.hospital.service.QuartoService;
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
@RequestMapping("/quartos")
public class QuartoController {

    private final QuartoService service;

    public QuartoController(QuartoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuartoResponse criar(@Valid @RequestBody QuartoRequest dados) {
        return service.criar(dados);
    }

    @GetMapping
    public List<QuartoResponse> listar(
            @RequestParam(required = false) Boolean disponiveis, @RequestParam(required = false) TipoQuarto tipo) {
        return service.listar(disponiveis, tipo);
    }

    @GetMapping("/{id}")
    public QuartoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public QuartoResponse atualizar(@PathVariable Long id, @Valid @RequestBody QuartoRequest dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
