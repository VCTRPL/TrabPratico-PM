package br.pucminas.hospital;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class ApiConsultasIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    // ---------- Consulta de rotina ----------

    @Test
    void agendaConsultaDeRotina() throws Exception {
        long paciente = criarPaciente("111.111.111-11");
        long clinico = criarProfissional("CLINICO_GERAL", "CRM-1", null);

        enviar("/consultas", rotina(paciente, clinico, "09:00"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ROTINA"))
                .andExpect(jsonPath("$.situacao").value("AGENDADA"))
                .andExpect(jsonPath("$.motivo").value("Check-up anual"))
                .andExpect(jsonPath("$.observacoesMedicas").doesNotExist());
    }

    @Test
    void consultaDeRotinaExigeMotivo() throws Exception {
        long paciente = criarPaciente("111.111.111-11");
        long clinico = criarProfissional("CLINICO_GERAL", "CRM-1", null);

        enviar("/consultas", """
                {"tipo":"ROTINA","pacienteId":%d,"profissionalId":%d,"data":"2026-12-01","horario":"09:00"}
                """.formatted(paciente, clinico))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void observacoesSoDepoisDaRealizacao() throws Exception {
        long consulta = criarRotina();

        alterar("/consultas/" + consulta + "/observacoes", "{\"observacoesMedicas\":\"Pressão normal\"}")
                .andExpect(status().isUnprocessableEntity());

        mudarSituacao(consulta, "EM_ATENDIMENTO").andExpect(status().isOk());
        alterar("/consultas/" + consulta + "/situacao",
                "{\"situacao\":\"CONCLUIDA\",\"observacoesMedicas\":\"Pressão normal\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CONCLUIDA"))
                .andExpect(jsonPath("$.observacoesMedicas").value("Pressão normal"));
    }

    // ---------- Consulta especializada ----------

    @Test
    void especialistaDaAreaAtendeConsultaEspecializada() throws Exception {
        long paciente = criarPaciente("111.111.111-11");
        long cardiologista = criarProfissional("ESPECIALISTA", "CRM-1", "Cardiologia");

        enviar("/consultas", especializada(paciente, cardiologista, "CARDIOLÓGIA"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ESPECIALIZADA"));
    }

    @Test
    void recusaProfissionalDeAreaIncompativel() throws Exception {
        long paciente = criarPaciente("111.111.111-11");
        long ortopedista = criarProfissional("ESPECIALISTA", "CRM-1", "Ortopedia");
        long clinico = criarProfissional("CLINICO_GERAL", "CRM-2", null);

        enviar("/consultas", especializada(paciente, ortopedista, "Cardiologia"))
                .andExpect(status().isUnprocessableEntity());
        enviar("/consultas", especializada(paciente, clinico, "Cardiologia"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void consultaDeRotinaEhDoClinicoGeral() throws Exception {
        long paciente = criarPaciente("111.111.111-11");
        long cardiologista = criarProfissional("ESPECIALISTA", "CRM-1", "Cardiologia");

        enviar("/consultas", rotina(paciente, cardiologista, "09:00")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void consultaEspecializadaRegistraObservacoes() throws Exception {
        long paciente = criarPaciente("111.111.111-11");
        long cardiologista = criarProfissional("ESPECIALISTA", "CRM-1", "Cardiologia");
        long consulta = idDe(enviar("/consultas", especializada(paciente, cardiologista, "Cardiologia")));

        mudarSituacao(consulta, "EM_ATENDIMENTO").andExpect(status().isOk());
        mudarSituacao(consulta, "CONCLUIDA").andExpect(status().isOk());
        alterar("/consultas/" + consulta + "/observacoes", "{\"observacoesMedicas\":\"Solicitado ecocardiograma\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especialidade").value("Cardiologia"))
                .andExpect(jsonPath("$.observacoesMedicas").value("Solicitado ecocardiograma"));
    }

    // ---------- Emergência ----------

    @Test
    void atendimentoDeEmergenciaSegueAguardandoIniciadoConcluido() throws Exception {
        long paciente = criarPaciente("111.111.111-11");
        long clinico = criarProfissional("CLINICO_GERAL", "CRM-1", null);

        long emergencia = idDe(enviar("/consultas", """
                {"tipo":"EMERGENCIA","pacienteId":%d,"profissionalId":%d,
                 "queixa":"Dor no peito","prioridade":"CRITICA"}
                """.formatted(paciente, clinico))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.situacao").value("AGUARDANDO"))
                .andExpect(jsonPath("$.prioridade").value("CRITICA"))
                .andExpect(jsonPath("$.data").exists()));

        mudarSituacao(emergencia, "CONCLUIDO").andExpect(status().isUnprocessableEntity());
        mudarSituacao(emergencia, "INICIADO").andExpect(status().isOk());
        mudarSituacao(emergencia, "CONCLUIDO").andExpect(status().isOk());
        mudarSituacao(emergencia, "AGUARDANDO").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void recusaPrioridadeDesconhecida() throws Exception {
        long paciente = criarPaciente("111.111.111-11");
        long clinico = criarProfissional("CLINICO_GERAL", "CRM-1", null);

        enviar("/consultas", """
                {"tipo":"EMERGENCIA","pacienteId":%d,"profissionalId":%d,"queixa":"Febre","prioridade":"URGENTE"}
                """.formatted(paciente, clinico))
                .andExpect(status().isBadRequest());
    }

    // ---------- Situações da consulta ----------

    @Test
    void concluidaNaoVoltaParaAgendada() throws Exception {
        long consulta = criarRotina();
        mudarSituacao(consulta, "EM_ATENDIMENTO").andExpect(status().isOk());
        mudarSituacao(consulta, "CONCLUIDA").andExpect(status().isOk());

        mudarSituacao(consulta, "AGENDADA").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void canceladaNaoViraConcluida() throws Exception {
        long consulta = criarRotina();
        mudarSituacao(consulta, "CANCELADA").andExpect(status().isOk());

        mudarSituacao(consulta, "CONCLUIDA").andExpect(status().isUnprocessableEntity());
        mudarSituacao(consulta, "EM_ATENDIMENTO").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void canceladaSoConcluiDepoisDeNovoAgendamento() throws Exception {
        long consulta = criarRotina();
        mudarSituacao(consulta, "CANCELADA").andExpect(status().isOk());

        alterar("/consultas/" + consulta + "/reagendamento", "{\"data\":\"2026-12-02\",\"horario\":\"11:00\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("AGENDADA"))
                .andExpect(jsonPath("$.data").value("2026-12-02"));
        mudarSituacao(consulta, "EM_ATENDIMENTO").andExpect(status().isOk());
        mudarSituacao(consulta, "CONCLUIDA").andExpect(status().isOk());
    }

    @Test
    void concluidaNaoPodeSerReagendada() throws Exception {
        long consulta = criarRotina();
        mudarSituacao(consulta, "EM_ATENDIMENTO").andExpect(status().isOk());
        mudarSituacao(consulta, "CONCLUIDA").andExpect(status().isOk());

        alterar("/consultas/" + consulta + "/reagendamento", "{\"data\":\"2026-12-02\",\"horario\":\"11:00\"}")
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void reagendarRespeitaAgendaDoProfissional() throws Exception {
        long maria = criarPaciente("111.111.111-11");
        long joao = criarPaciente("222.222.222-22");
        long clinico = criarProfissional("CLINICO_GERAL", "CRM-1", null);
        enviar("/consultas", rotina(maria, clinico, "09:00")).andExpect(status().isCreated());
        long consulta = idDe(enviar("/consultas", rotina(joao, clinico, "10:00")));

        alterar("/consultas/" + consulta + "/reagendamento", "{\"data\":\"2026-12-01\",\"horario\":\"09:00\"}")
                .andExpect(status().isConflict());
        alterar("/consultas/" + consulta + "/reagendamento", "{\"data\":\"2026-12-01\",\"horario\":\"10:15\"}")
                .andExpect(status().isOk());
    }

    @Test
    void naoPulaDeAgendadaParaConcluida() throws Exception {
        long consulta = criarRotina();

        mudarSituacao(consulta, "CONCLUIDA").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cicloCompletoComConfirmacao() throws Exception {
        long consulta = criarRotina();

        mudarSituacao(consulta, "confirmada").andExpect(status().isOk());
        mudarSituacao(consulta, "EM_ATENDIMENTO").andExpect(status().isOk());
        mudarSituacao(consulta, "CONCLUIDA").andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CONCLUIDA"));
    }

    @Test
    void recusaSituacaoInexistente() throws Exception {
        mudarSituacao(criarRotina(), "ADIADA").andExpect(status().isUnprocessableEntity());
    }

    // ---------- Regras da Sprint 1 ----------

    @Test
    void consultaExigePacienteEProfissional() throws Exception {
        enviar("/consultas", """
                {"tipo":"ROTINA","data":"2026-12-01","horario":"09:00","motivo":"Check-up"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.pacienteId").exists())
                .andExpect(jsonPath("$.campos.profissionalId").exists());
    }

    @Test
    void pacienteOuProfissionalInexistenteRetorna404() throws Exception {
        long paciente = criarPaciente("111.111.111-11");
        long clinico = criarProfissional("CLINICO_GERAL", "CRM-1", null);

        enviar("/consultas", rotina(999999, clinico, "09:00")).andExpect(status().isNotFound());
        enviar("/consultas", rotina(paciente, 999999, "09:00")).andExpect(status().isNotFound());
    }

    @Test
    void profissionalNaoTemDoisAtendimentosNoMesmoHorario() throws Exception {
        long maria = criarPaciente("111.111.111-11");
        long joao = criarPaciente("222.222.222-22");
        long clinico = criarProfissional("CLINICO_GERAL", "CRM-1", null);

        enviar("/consultas", rotina(maria, clinico, "09:00")).andExpect(status().isCreated());
        enviar("/consultas", rotina(joao, clinico, "09:00")).andExpect(status().isConflict());
        enviar("/consultas", rotina(joao, clinico, "10:00")).andExpect(status().isCreated());
    }

    @Test
    void identificaEmergenciasPorSituacao() throws Exception {
        long paciente = criarPaciente("111.111.111-11");
        long clinico = criarProfissional("CLINICO_GERAL", "CRM-1", null);
        String emergencia = """
                {"tipo":"EMERGENCIA","pacienteId":%d,"profissionalId":%d,"data":"2026-12-01","horario":"%s",
                 "queixa":"Febre","prioridade":"BAIXA"}
                """;
        long iniciada = idDe(enviar("/consultas", emergencia.formatted(paciente, clinico, "09:00")));
        enviar("/consultas", emergencia.formatted(paciente, clinico, "09:10")).andExpect(status().isCreated());
        mudarSituacao(iniciada, "INICIADO").andExpect(status().isOk());

        mvc.perform(get("/consultas").param("tipo", "EMERGENCIA").param("situacao", "aguardando"))
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/consultas").param("tipo", "EMERGENCIA").param("situacao", "INICIADO"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(iniciada));
    }

    @Test
    void cancelarLiberaOHorario() throws Exception {
        long maria = criarPaciente("111.111.111-11");
        long joao = criarPaciente("222.222.222-22");
        long clinico = criarProfissional("CLINICO_GERAL", "CRM-1", null);

        long consulta = idDe(enviar("/consultas", rotina(maria, clinico, "09:00")).andExpect(status().isCreated()));
        mudarSituacao(consulta, "CANCELADA").andExpect(status().isOk());

        enviar("/consultas", rotina(joao, clinico, "09:00")).andExpect(status().isCreated());
    }

    @Test
    void agendaDoProfissionalReflereAsConsultas() throws Exception {
        long paciente = criarPaciente("111.111.111-11");
        long clinico = criarProfissional("CLINICO_GERAL", "CRM-1", null);
        enviar("/consultas", rotina(paciente, clinico, "09:00")).andExpect(status().isCreated());

        mvc.perform(get("/profissionais").param("data", "2026-12-01").param("horario", "09:00"))
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/profissionais").param("data", "2026-12-01").param("horario", "10:00"))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void listaEFiltraConsultas() throws Exception {
        long paciente = criarPaciente("111.111.111-11");
        long clinico = criarProfissional("CLINICO_GERAL", "CRM-1", null);
        long cardiologista = criarProfissional("ESPECIALISTA", "CRM-2", "Cardiologia");
        enviar("/consultas", rotina(paciente, clinico, "09:00")).andExpect(status().isCreated());
        enviar("/consultas", especializada(paciente, cardiologista, "Cardiologia")).andExpect(status().isCreated());

        mvc.perform(get("/consultas")).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/consultas").param("tipo", "ESPECIALIZADA")).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/consultas").param("profissionalId", String.valueOf(clinico)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo").value("ROTINA"));
    }

    @Test
    void consultaInexistenteRetorna404() throws Exception {
        mvc.perform(get("/consultas/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Consulta 999999 não encontrada."));
    }

    // ---------- Auxiliares ----------

    private long criarRotina() throws Exception {
        long paciente = criarPaciente("111.111.111-11");
        long clinico = criarProfissional("CLINICO_GERAL", "CRM-1", null);
        return idDe(enviar("/consultas", rotina(paciente, clinico, "09:00")).andExpect(status().isCreated()));
    }

    private ResultActions mudarSituacao(long consulta, String situacao) throws Exception {
        return alterar("/consultas/" + consulta + "/situacao", "{\"situacao\":\"" + situacao + "\"}");
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
                {"tipo":"%s","nome":"Dra. Ana Lima","registroProfissional":"%s",
                 "telefone":"(31) 91111-0001","email":"ana@hospital.com","areaAtuacao":%s}
                """.formatted(tipo, registro, valorArea)).andExpect(status().isCreated()));
    }

    private String rotina(long paciente, long profissional, String horario) {
        return """
                {"tipo":"ROTINA","pacienteId":%d,"profissionalId":%d,
                 "data":"2026-12-01","horario":"%s","motivo":"Check-up anual"}
                """.formatted(paciente, profissional, horario);
    }

    private String especializada(long paciente, long profissional, String especialidade) {
        return """
                {"tipo":"ESPECIALIZADA","pacienteId":%d,"profissionalId":%d,
                 "data":"2026-12-01","horario":"14:00","especialidade":"%s"}
                """.formatted(paciente, profissional, especialidade);
    }

    private long idDe(ResultActions resultado) throws Exception {
        return json.readTree(resultado.andReturn().getResponse().getContentAsString()).get("id").asLong();
    }

    private ResultActions enviar(String rota, String corpo) throws Exception {
        return mvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions alterar(String rota, String corpo) throws Exception {
        return mvc.perform(patch(rota).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }
}
