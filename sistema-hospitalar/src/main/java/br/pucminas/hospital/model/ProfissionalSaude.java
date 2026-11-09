package br.pucminas.hospital.model;

import br.pucminas.hospital.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import java.text.Normalizer;
import java.util.Locale;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ProfissionalSaude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String registroProfissional;

    @Column(nullable = false)
    private String telefone;

    @Column(nullable = false)
    private String email;

    protected ProfissionalSaude() {
    }

    protected ProfissionalSaude(String nome, String registroProfissional, String telefone, String email) {
        atualizarDados(nome, registroProfissional, telefone, email);
    }

    public final void atualizarDados(String nome, String registroProfissional, String telefone, String email) {
        this.nome = nome;
        this.registroProfissional = registroProfissional;
        this.telefone = telefone;
        this.email = email;
    }

    public abstract TipoProfissional getTipo();

    public abstract String getAreaAtuacao();

    public abstract void definirAreaAtuacao(String area);

    /** Compara ignorando maiúsculas e acentos: "CARDIOLOGIA" e "cardiologia" são a mesma área. */
    public boolean atuaNaArea(String area) {
        return area != null && normalizar(getAreaAtuacao()).equals(normalizar(area));
    }

    protected static String exigirArea(String area, String mensagem) {
        if (area == null || area.isBlank()) {
            throw new RegraNegocioException(mensagem);
        }
        return area.trim();
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getRegistroProfissional() {
        return registroProfissional;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }
}
