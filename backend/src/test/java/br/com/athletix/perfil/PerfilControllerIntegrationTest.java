package br.com.athletix.perfil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class PerfilControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void updatesOwnProfileAndRecordsActivityFromServerContext() throws Exception {
        MvcResult csrfResult = mockMvc.perform(get("/auth/csrf")).andExpect(status().isOk()).andReturn();
        JsonNode csrf = objectMapper.readTree(csrfResult.getResponse().getContentAsString());
        MvcResult login = mockMvc.perform(post("/auth/login")
                        .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                        .header(csrf.get("headerName").asText(), csrf.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@athletix.local\",\"senha\":\"Athletix@2026\"}"))
                .andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        MvcResult perfil = mockMvc.perform(get("/perfil").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@athletix.local"))
                .andReturn();
        JsonNode perfilJson = objectMapper.readTree(perfil.getResponse().getContentAsString());

        mockMvc.perform(put("/perfil").session(session)
                        .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                        .header(csrf.get("headerName").asText(), csrf.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Administrador Atualizado","telefone":"11999999999","curso":"Engenharia",
                                 "periodoCurso":4,"usuarioVersion":%d,"membroVersion":%d}
                                """.formatted(perfilJson.get("usuarioVersion").asLong(), perfilJson.get("membroVersion").asLong())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Administrador Atualizado"))
                .andExpect(jsonPath("$.email").value("admin@athletix.local"));

        MvcResult atividades = mockMvc.perform(get("/perfil/atividades").session(session))
                .andExpect(status().isOk()).andReturn();
        assertTrue(atividades.getResponse().getContentAsString().contains("\"acao\":\"PERFIL_ATUALIZADO\""));
    }

    @Test
    void onlyAppliesRequestedLinkChangeAfterAdministrativeDecision() throws Exception {
        MvcResult csrfResult = mockMvc.perform(get("/auth/csrf")).andExpect(status().isOk()).andReturn();
        JsonNode csrf = objectMapper.readTree(csrfResult.getResponse().getContentAsString());
        MvcResult login = mockMvc.perform(post("/auth/login")
                        .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                        .header(csrf.get("headerName").asText(), csrf.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@athletix.local\",\"senha\":\"Athletix@2026\"}"))
                .andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        MvcResult perfil = mockMvc.perform(get("/perfil").session(session)).andExpect(status().isOk()).andReturn();
        JsonNode perfilJson = objectMapper.readTree(perfil.getResponse().getContentAsString());

        MvcResult cargo = mockMvc.perform(post("/cargos").session(session)
                        .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                        .header(csrf.get("headerName").asText(), csrf.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Coordenação\",\"descricao\":\"Cargo solicitado\",\"perfilAcesso\":\"GESTOR\",\"ativo\":true}"))
                .andExpect(status().isCreated()).andReturn();
        long cargoId = objectMapper.readTree(cargo.getResponse().getContentAsString()).get("id").asLong();

        MvcResult solicitacao = mockMvc.perform(post("/perfil/solicitacoes-vinculo").session(session)
                        .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                        .header(csrf.get("headerName").asText(), csrf.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cargoPretendidoId":%d,"departamentoPretendidoId":%d,"motivo":"Assumir coordenação"}
                                """.formatted(cargoId, perfilJson.get("departamentoId").asLong())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn();
        JsonNode solicitacaoJson = objectMapper.readTree(solicitacao.getResponse().getContentAsString());

        mockMvc.perform(get("/membros/solicitacoes-vinculo?status=PENDENTE").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").isNumber());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                        "/membros/solicitacoes-vinculo/{id}/decisao", solicitacaoJson.get("id").asLong())
                        .session(session)
                        .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                        .header(csrf.get("headerName").asText(), csrf.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"decisao":"APROVAR","version":%d,"membroVersion":%d}
                                """.formatted(solicitacaoJson.get("version").asLong(), perfilJson.get("membroVersion").asLong())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"));

        mockMvc.perform(get("/perfil").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cargoId").value(cargoId));
    }
}
