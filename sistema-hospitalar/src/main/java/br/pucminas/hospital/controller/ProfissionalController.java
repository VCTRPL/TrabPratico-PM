package br.pucminas.hospital.controller;

import br.pucminas.hospital.dto.EncaminhamentoRequest;
import br.pucminas.hospital.dto.EncaminhamentoResponse;
import br.pucminas.hospital.dto.ProfissionalRequest;
import br.pucminas.hospital.dto.ProfissionalResponse;
import br.pucminas.hospital.service.ProfissionalService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
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
@RequestMapping("/profissionais")
public class ProfissionalController {

    private final ProfissionalService service;

    public ProfissionalController(ProfissionalService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfissionalResponse criar(@Valid @RequestBody ProfissionalRequest dados) {
        return service.criar(dados);
    }

    @GetMapping
    public List<ProfissionalResponse> listar(
            @RequestParam(required = false) String area,
            @RequestParam(required = false) LocalDate data,
            @RequestParam(required = false) LocalTime horario) {
        return service.listar(area, data, horario);
    }

    @GetMapping("/{id}")
    public ProfissionalResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public ProfissionalResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProfissionalRequest dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @PostMapping("/{id}/encaminhamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public EncaminhamentoResponse encaminhar(@PathVariable Long id, @Valid @RequestBody EncaminhamentoRequest dados) {
        return service.encaminhar(id, dados);
    }

    @GetMapping("/{id}/encaminhamentos")
    public List<EncaminhamentoResponse> listarEncaminhamentos(@PathVariable Long id) {
        return service.listarEncaminhamentos(id);
    }
}
