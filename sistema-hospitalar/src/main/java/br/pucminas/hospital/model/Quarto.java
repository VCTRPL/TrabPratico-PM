package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

/**
 * Quarto do hospital. A ocupação é controlada aqui para que a capacidade máxima nunca seja ultrapassada (RN5):
 * só uma {@link Internacao} ocupa ou libera vaga, e a situação é recalculada a cada mudança.
 */
@Entity
public class Quarto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int numero;

    @Column(nullable = false)
    private int andar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoQuarto tipo;

    @Column(nullable = false)
    private int capacidadeMaxima;

    @Column(nullable = false)
    private int ocupacaoAtual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoQuarto situacao;

    /** Impede que duas internações simultâneas ocupem a última vaga do mesmo quarto. */
    @Version
    private Long versao;

    protected Quarto() {
    }

    public Quarto(int numero, int andar, TipoQuarto tipo, int capacidadeMaxima) {
        atualizarDados(numero, andar, tipo, capacidadeMaxima);
    }

    public final void atualizarDados(int numero, int andar, TipoQuarto tipo, int capacidadeMaxima) {
        if (tipo == null) {
            throw new RegraNegocioException("O quarto exige um tipo.");
        }
        if (capacidadeMaxima < 1) {
            throw new RegraNegocioException("A capacidade máxima do quarto deve ser de pelo menos 1 leito.");
        }
        if (capacidadeMaxima < ocupacaoAtual) {
            throw new RegraNegocioException("A capacidade máxima não pode ser menor que a ocupação atual ("
                    + ocupacaoAtual + " paciente(s)).");
        }
        this.numero = numero;
        this.andar = andar;
        this.tipo = tipo;
        this.capacidadeMaxima = capacidadeMaxima;
        recalcularSituacao();
    }

    public boolean temVaga() {
        return ocupacaoAtual < capacidadeMaxima;
    }

    public int ocupacaoAtual() {
        return ocupacaoAtual;
    }

    /** Visível só no pacote: a vaga é ocupada ao criar uma {@link Internacao}. */
    void ocuparVaga() {
        if (!temVaga()) {
            throw new RegraNegocioException("O quarto " + numero + " não está disponível: capacidade máxima de "
                    + capacidadeMaxima + " paciente(s) atingida.");
        }
        ocupacaoAtual++;
        recalcularSituacao();
    }

    /** Visível só no pacote: a vaga é liberada na alta ou na exclusão de uma {@link Internacao} ativa. */
    void liberarVaga() {
        if (ocupacaoAtual > 0) {
            ocupacaoAtual--;
        }
        recalcularSituacao();
    }

    private void recalcularSituacao() {
        this.situacao = temVaga() ? SituacaoQuarto.DISPONIVEL : SituacaoQuarto.OCUPADO;
    }

    public Long getId() {
        return id;
    }

    public int getNumero() {
        return numero;
    }

    public int getAndar() {
        return andar;
    }

    public TipoQuarto getTipo() {
        return tipo;
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public SituacaoQuarto getSituacao() {
        return situacao;
    }
}
