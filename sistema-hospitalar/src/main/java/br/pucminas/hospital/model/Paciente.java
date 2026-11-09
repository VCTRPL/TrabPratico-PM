package br.pucminas.hospital.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;

@Entity
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private String telefone;

    @Column(nullable = false)
    private String endereco;

    @Column(nullable = false)
    private String email;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "historico_id", nullable = false, unique = true)
    private HistoricoMedico historico;

    protected Paciente() {
    }

    public Paciente(String nome, String cpf, LocalDate dataNascimento, String telefone, String endereco, String email) {
        atualizarDados(nome, cpf, dataNascimento, telefone, endereco, email);
        this.historico = HistoricoMedico.vazio();
    }

    public final void atualizarDados(
            String nome, String cpf, LocalDate dataNascimento, String telefone, String endereco, String email) {
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.endereco = endereco;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getEmail() {
        return email;
    }

    public HistoricoMedico getHistorico() {
        return historico;
    }
}
