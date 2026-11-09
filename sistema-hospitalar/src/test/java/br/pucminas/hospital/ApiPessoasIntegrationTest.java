package br.pucminas.hospital;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiPessoasIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    void cadastraPacienteComHistoricoVazio() throws Exception {
        long id = criarPaciente("111.111.111-11");

        mvc.perform(get("/pacientes/" + id + "/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.informacoesRelevantes", hasSize(0)));
    }

    @Test
    void adicionaInformacaoAoHistorico() throws Exception {
        long id = criarPaciente("111.111.111-11");

        enviar("/pacientes/" + id + "/historico", "{\"informacao\":\"Alergia a dipirona\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.informacoesRelevantes[0]").value("Alergia a dipirona"));
    }

    @Test
    void recusaCpfDuplicado() throws Exception {
        criarPaciente("111.111.111-11");

        enviar("/pacientes", paciente("111.111.111-11")).andExpect(status().isConflict());
    }

    @Test
    void recusaPacienteInvalidoApontandoOCampo() throws Exception {
        enviar("/pacientes", paciente("123")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cpf").exists());
    }

    @Test
    void pacienteInexistenteRetorna404() throws Exception {
        mvc.perform(get("/pacientes/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Paciente 999999 não encontrado."));
    }

    @Test
    void especialistaExigeEspecialidade() throws Exception {
        enviar("/profissionais", profissional("ESPECIALISTA", "CRM-1", null))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cirurgiaoExigeAreaCirurgica() throws Exception {
        enviar("/profissionais", profissional("CIRURGIAO", "CRM-1", " "))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void recusaRegistroDuplicado() throws Exception {
        criarProfissional("CLINICO_GERAL", "CRM-1", null);

        enviar("/profissionais", profissional("CLINICO_GERAL", "CRM-1", null)).andExpect(status().isConflict());
    }

    @Test
    void recusaCategoriaDesconhecida() throws Exception {
        enviar("/profissionais", profissional("ENFERMEIRO", "CRM-1", null)).andExpect(status().isBadRequest());
    }

    @Test
    void buscaPorAreaIgnoraMaiusculasEAcentos() throws Exception {
        criarProfissional("ESPECIALISTA", "CRM-1", "Cardiologia");
        criarProfissional("ESPECIALISTA", "CRM-2", "Ortopedia");
        criarProfissional("CLINICO_GERAL", "CRM-3", null);

        mvc.perform(get("/profissionais").param("area", "CARDIOLÓGIA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].registroProfissional").value("CRM-1"));
    }

    @Test
    void disponibilidadeExigeDataEHorarioJuntos() throws Exception {
        mvc.perform(get("/profissionais").param("data", "2026-12-01")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void naoPermiteMudarCategoria() throws Exception {
        long id = criarProfissional("ESPECIALISTA", "CRM-1", "Cardiologia");

        mvc.perform(put("/profissionais/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(profissional("CIRURGIAO", "CRM-1", "Cardíaca")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void clinicoEncaminhaPaciente() throws Exception {
        long clinico = criarProfissional("CLINICO_GERAL", "CRM-1", null);
        long paciente = criarPaciente("111.111.111-11");

        enviar("/profissionais/" + clinico + "/encaminhamentos", encaminhamento(paciente))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.especialidadeDestino").value("Cardiologia"));

        mvc.perform(get("/profissionais/" + clinico + "/encaminhamentos"))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void apenasClinicoEncaminha() throws Exception {
        long especialista = criarProfissional("ESPECIALISTA", "CRM-1", "Cardiologia");
        long paciente = criarPaciente("111.111.111-11");

        enviar("/profissionais/" + especialista + "/encaminhamentos", encaminhamento(paciente))
                .andExpect(status().isUnprocessableEntity());
    }

    private long criarPaciente(String cpf) throws Exception {
        return idDe(enviar("/pacientes", paciente(cpf)).andExpect(status().isCreated()));
    }

    private long criarProfissional(String tipo, String registro, String area) throws Exception {
        return idDe(enviar("/profissionais", profissional(tipo, registro, area)).andExpect(status().isCreated()));
    }

    private long idDe(ResultActions resultado) throws Exception {
        return json.readTree(resultado.andReturn().getResponse().getContentAsString()).get("id").asLong();
    }

    private ResultActions enviar(String rota, String corpo) throws Exception {
        return mvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private String paciente(String cpf) {
        return """
                {"nome":"Maria Souza","cpf":"%s","dataNascimento":"1985-03-12",
                 "telefone":"(31) 90000-0001","endereco":"Rua A, 10","email":"maria@email.com"}
                """.formatted(cpf);
    }

    private String profissional(String tipo, String registro, String area) {
        String valorArea = area == null ? "null" : "\"" + area + "\"";
        return """
                {"tipo":"%s","nome":"Dra. Ana Lima","registroProfissional":"%s",
                 "telefone":"(31) 91111-0001","email":"ana@hospital.com","areaAtuacao":%s}
                """.formatted(tipo, registro, valorArea);
    }

    private String encaminhamento(long pacienteId) {
        return """
                {"pacienteId":%d,"especialidadeDestino":"Cardiologia","motivo":"Sopro cardíaco"}
                """.formatted(pacienteId);
    }
}
