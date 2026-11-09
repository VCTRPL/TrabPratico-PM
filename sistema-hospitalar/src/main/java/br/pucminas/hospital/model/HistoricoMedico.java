package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import java.util.ArrayList;
import java.util.List;

@Entity
public class HistoricoMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "historico_informacoes", joinColumns = @JoinColumn(name = "historico_id"))
    @OrderColumn(name = "posicao")
    @Column(name = "informacao", nullable = false, length = 1000)
    private List<String> informacoesRelevantes = new ArrayList<>();

    protected HistoricoMedico() {
    }

    static HistoricoMedico vazio() {
        return new HistoricoMedico();
    }

    public void adicionarInformacao(String informacao) {
        if (informacao == null || informacao.isBlank()) {
            throw new RegraNegocioException("A informação do histórico não pode ser vazia.");
        }
        informacoesRelevantes.add(informacao.trim());
    }

    public Long getId() {
        return id;
    }

    public List<String> getInformacoesRelevantes() {
        return List.copyOf(informacoesRelevantes);
    }
}
