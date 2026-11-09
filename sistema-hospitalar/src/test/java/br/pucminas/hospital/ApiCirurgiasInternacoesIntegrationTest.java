package br.pucminas.hospital;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class ApiCirurgiasInternacoesIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    // ---------- Procedimentos cirúrgicos ----------

    @Test
    void cirurgiaoRegistraProcedimentoAgendado() throws Exception {
        long cirurgiao = criarProfissional("CIRURGIAO", "CRM-1", "Cardíaca");
        long paciente = criarPaciente("111.111.111-11");

        enviar("/profissionais/" + cirurgiao + "/procedimentos", procedimento(paciente))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADO"))
                .andExpect(jsonPath("$.pacienteNome").value("Maria Souza"))
                .andExpect(jsonPath("$.tipoCirurgia").value("Cardíaca"))
                .andExpect(jsonPath("$.inicioPrevisto").value("2026-12-01T08:00:00"));

        mvc.perform(get("/profissionais/" + cirurgiao + "/procedimentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao").value("Troca de válvula mitral"));
    }

    @Test
    void apenasCirurgiaoTemProcedimentos() throws Exception {
        long clinico = criarProfissional("CLINICO_GERAL", "CRM-1", null);
        long paciente = criarPaciente("111.111.111-11");

        enviar("/profissionais/" + clinico + "/procedimentos", procedimento(paciente))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/profissionais/" + clinico + "/procedimentos")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void procedimentoExigePacienteExistente() throws Exception {
        long cirurgiao = criarProfissional("CIRURGIAO", "CRM-1", "Cardíaca");

        enviar("/profissionais/" + cirurgiao + "/procedimentos", procedimento(999999))
                .andExpect(status().isNotFound());
    }

    @Test
    void naoConcluiProcedimentoNaoIniciado() throws Exception {
        long procedimento = criarProcedimento();

        enviar("/procedimentos/" + procedimento + "/concluir", "{}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value(
                        "Não é possível concluir um procedimento que não foi iniciado (status atual: AGENDADO)."));
    }

    @Test
    void cicloCompletoDoProcedimento() throws Exception {
        long procedimento = criarProcedimento();

        enviar("/procedimentos/" + procedimento + "/iniciar", "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.inicioEfetivo").exists());

        enviar("/procedimentos/" + procedimento + "/concluir", "{\"observacoesPosOperatorias\":\"Sem intercorrências\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"))
                .andExpect(jsonPath("$.terminoEfetivo").exists())
                .andExpect(jsonPath("$.observacoesPosOperatorias").value("Sem intercorrências"));

        enviar("/procedimentos/" + procedimento + "/cancelar", "").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void procedimentoCanceladoNaoPodeSerIniciadoNemConcluido() throws Exception {
        long procedimento = criarProcedimento();

        enviar("/procedimentos/" + procedimento + "/cancelar", "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
        enviar("/procedimentos/" + procedimento + "/iniciar", "").andExpect(status().isUnprocessableEntity());
        enviar("/procedimentos/" + procedimento + "/concluir", "").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void filtraProcedimentosPorStatus() throws Exception {
        long cirurgiao = criarProfissional("CIRURGIAO", "CRM-1", "Cardíaca");
        long paciente = criarPaciente("111.111.111-11");
        long primeiro = idDe(enviar("/profissionais/" + cirurgiao + "/procedimentos", procedimento(paciente)));
        enviar("/profissionais/" + cirurgiao + "/procedimentos", procedimento(paciente));
        enviar("/procedimentos/" + primeiro + "/cancelar", "");

        mvc.perform(get("/profissionais/" + cirurgiao + "/procedimentos").param("status", "AGENDADO"))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ---------- Quartos ----------

    @Test
    void cadastraQuartoDisponivel() throws Exception {
        enviar("/quartos", quarto(101, "ENFERMARIA", 2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ENFERMARIA"))
                .andExpect(jsonPath("$.situacao").value("DISPONIVEL"))
                .andExpect(jsonPath("$.ocupacaoAtual").value(0))
                .andExpect(jsonPath("$.vagasLivres").value(2));
    }

    @Test
    void recusaQuartoComNumeroRepetido() throws Exception {
        criarQuarto(101, 1);

        enviar("/quartos", quarto(101, "UTI", 1)).andExpect(status().isConflict());
    }

    @Test
    void recusaQuartoSemCapacidadeOuTipoInvalido() throws Exception {
        enviar("/quartos", quarto(101, "ENFERMARIA", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.capacidadeMaxima").exists());
        enviar("/quartos", quarto(101, "SUITE", 1)).andExpect(status().isBadRequest());
    }

    // ---------- Internações ----------

    @Test
    void internacaoOcupaVagaEAltaLibera() throws Exception {
        long quarto = criarQuarto(101, 1);
        long internacao = idDe(internar(criarPaciente("111.111.111-11"), quarto).andExpect(status().isCreated())
                .andExpect(jsonPath("$.ativa").value(true))
                .andExpect(jsonPath("$.quartoNumero").value(101)));

        mvc.perform(get("/quartos/" + quarto))
                .andExpect(jsonPath("$.situacao").value("OCUPADO"))
                .andExpect(jsonPath("$.ocupacaoAtual").value(1));

        enviar("/internacoes/" + internacao + "/alta", "{\"dataAlta\":\"2026-11-05\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativa").value(false))
                .andExpect(jsonPath("$.dataEfetivaAlta").value("2026-11-05"));

        mvc.perform(get("/quartos/" + quarto)).andExpect(jsonPath("$.situacao").value("DISPONIVEL"));
    }

    @Test
    void quartoNaoUltrapassaCapacidadeMaxima() throws Exception {
        long quarto = criarQuarto(101, 1);
        internar(criarPaciente("111.111.111-11"), quarto).andExpect(status().isCreated());

        internar(criarPaciente("222.222.222-22"), quarto).andExpect(status().isUnprocessableEntity());

        mvc.perform(get("/quartos").param("disponiveis", "true")).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void internacaoExigePacienteEQuarto() throws Exception {
        long quarto = criarQuarto(101, 1);
        long medico = criarProfissional("CLINICO_GERAL", "CRM-9", null);

        enviar("/internacoes", """
                {"profissionalId":%d,"quartoId":%d,"dataEntrada":"2026-11-01"}
                """.formatted(medico, quarto))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.pacienteId").exists());
        internar(criarPaciente("111.111.111-11"), 999999).andExpect(status().isNotFound());
    }

    @Test
    void pacienteNaoTemDuasInternacoesAtivas() throws Exception {
        long paciente = criarPaciente("111.111.111-11");
        internar(paciente, criarQuarto(101, 2)).andExpect(status().isCreated());

        internar(paciente, criarQuarto(102, 2)).andExpect(status().isConflict());
    }

    @Test
    void naoReduzCapacidadeAbaixoDaOcupacao() throws Exception {
        long quarto = criarQuarto(101, 2);
        internar(criarPaciente("111.111.111-11"), quarto);
        internar(criarPaciente("222.222.222-22"), quarto);

        mvc.perform(put("/quartos/" + quarto).contentType(MediaType.APPLICATION_JSON)
                        .content(quarto(101, "ENFERMARIA", 1)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void naoExcluiQuartoComInternacoes() throws Exception {
        long quarto = criarQuarto(101, 1);
        internar(criarPaciente("111.111.111-11"), quarto);

        mvc.perform(delete("/quartos/" + quarto)).andExpect(status().isConflict());
    }

    @Test
    void excluirInternacaoAtivaLiberaVaga() throws Exception {
        long quarto = criarQuarto(101, 1);
        long internacao = idDe(internar(criarPaciente("111.111.111-11"), quarto));

        mvc.perform(delete("/internacoes/" + internacao)).andExpect(status().isNoContent());
        mvc.perform(get("/quartos/" + quarto)).andExpect(jsonPath("$.ocupacaoAtual").value(0));
    }

    @Test
    void altaNaoPodeSerAnteriorAEntradaNemRepetida() throws Exception {
        long internacao = idDe(internar(criarPaciente("111.111.111-11"), criarQuarto(101, 1)));

        enviar("/internacoes/" + internacao + "/alta", "{\"dataAlta\":\"2026-10-01\"}")
                .andExpect(status().isUnprocessableEntity());
        enviar("/internacoes/" + internacao + "/alta", "{\"dataAlta\":\"2026-11-05\"}").andExpect(status().isOk());
        enviar("/internacoes/" + internacao + "/alta", "{\"dataAlta\":\"2026-11-06\"}")
                .andExpect(status().isUnprocessableEntity());
    }

    // ---------- Auxiliares ----------

    private long criarProcedimento() throws Exception {
        long cirurgiao = criarProfissional("CIRURGIAO", "CRM-1", "Cardíaca");
        long paciente = criarPaciente("111.111.111-11");
        return idDe(enviar("/profissionais/" + cirurgiao + "/procedimentos", procedimento(paciente))
                .andExpect(status().isCreated()));
    }

    private ResultActions internar(long paciente, long quarto) throws Exception {
        long medico = criarProfissional("CLINICO_GERAL", "CRM-P" + paciente + "Q" + quarto, null);
        return enviar("/internacoes", """
                {"pacienteId":%d,"profissionalId":%d,"quartoId":%d,
                 "dataEntrada":"2026-11-01","dataPrevistaAlta":"2026-11-08","observacoes":"Pós-operatório"}
                """.formatted(paciente, medico, quarto));
    }

    private long criarQuarto(int numero, int capacidade) throws Exception {
        return idDe(enviar("/quartos", quarto(numero, "ENFERMARIA", capacidade)).andExpect(status().isCreated()));
    }

    private long criarPaciente(String cpf) throws Exception {
        return idDe(enviar("/pacientes", """
                {"nome":"Maria Souza","cpf":"%s","dataNascimento":"1985-03-12",
                 "telefone":"(31) 90000-0001","endereco":"Rua A, 10","email":"maria@email.com"}
                """.formatted(cpf)).andExpect(status().isCreated()));
    }

    private long criarProfissional(String tipo, String registro, String area) throws Exception {
        String valorArea = area == null ? "null" : "\"" + area + "\"";
        return idDe(enviar("/profissionais", """
                {"tipo":"%s","nome":"Dr. Carlos Dias","registroProfissional":"%s",
                 "telefone":"(31) 91111-0001","email":"carlos@hospital.com","areaAtuacao":%s}
                """.formatted(tipo, registro, valorArea)).andExpect(status().isCreated()));
    }

    private String procedimento(long pacienteId) {
        return """
                {"pacienteId":%d,"data":"2026-12-01","horario":"08:00","terminoPrevisto":"2026-12-01T12:00:00",
                 "descricao":"Troca de válvula mitral","tipoCirurgia":"Cardíaca",
                 "observacoesPreOperatorias":"Jejum de 8 horas"}
                """.formatted(pacienteId);
    }

    private String quarto(int numero, String tipo, int capacidade) {
        return """
                {"numero":%d,"andar":1,"tipo":"%s","capacidadeMaxima":%d}
                """.formatted(numero, tipo, capacidade);
    }

    private long idDe(ResultActions resultado) throws Exception {
        return json.readTree(resultado.andReturn().getResponse().getContentAsString()).get("id").asLong();
    }

    private ResultActions enviar(String rota, String corpo) throws Exception {
        return mvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }
}
