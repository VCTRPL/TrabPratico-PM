package br.pucminas.hospital.service;

import br.pucminas.hospital.dto.QuartoRequest;
import br.pucminas.hospital.dto.QuartoResponse;
import br.pucminas.hospital.exception.ConflitoException;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.model.Quarto;
import br.pucminas.hospital.model.TipoQuarto;
import br.pucminas.hospital.repository.InternacaoRepository;
import br.pucminas.hospital.repository.QuartoRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class QuartoService {

    private final QuartoRepository quartos;
    private final InternacaoRepository internacoes;

    public QuartoService(QuartoRepository quartos, InternacaoRepository internacoes) {
        this.quartos = quartos;
        this.internacoes = internacoes;
    }

    public QuartoResponse criar(QuartoRequest dados) {
        if (quartos.existsByNumero(dados.numero())) {
            throw new ConflitoException("Já existe um quarto com o número " + dados.numero() + ".");
        }
        Quarto quarto = new Quarto(dados.numero(), dados.andar(), dados.tipo(), dados.capacidadeMaxima());
        return QuartoResponse.de(quartos.save(quarto));
    }

    /** Com {@code disponiveis=true}, mantém só os quartos com vaga; {@code tipo} filtra pelo tipo de quarto. */
    @Transactional(readOnly = true)
    public List<QuartoResponse> listar(Boolean disponiveis, TipoQuarto tipo) {
        return quartos.findAll().stream()
                .filter(quarto -> !Boolean.TRUE.equals(disponiveis) || quarto.temVaga())
                .filter(quarto -> tipo == null || quarto.getTipo() == tipo)
                .sorted(Comparator.comparingInt(Quarto::getNumero))
                .map(QuartoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuartoResponse buscar(Long id) {
        return QuartoResponse.de(obter(id));
    }

    public QuartoResponse atualizar(Long id, QuartoRequest dados) {
        Quarto quarto = obter(id);
        if (quartos.existsByNumeroAndIdNot(dados.numero(), id)) {
            throw new ConflitoException("Já existe um quarto com o número " + dados.numero() + ".");
        }
        quarto.atualizarDados(dados.numero(), dados.andar(), dados.tipo(), dados.capacidadeMaxima());
        return QuartoResponse.de(quarto);
    }

    public void excluir(Long id) {
        Quarto quarto = obter(id);
        if (internacoes.existsByQuartoId(id)) {
            throw new ConflitoException("O quarto " + quarto.getNumero() + " possui internações registradas.");
        }
        quartos.delete(quarto);
    }

    private Quarto obter(Long id) {
        return quartos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quarto " + id + " não encontrado."));
    }
}
