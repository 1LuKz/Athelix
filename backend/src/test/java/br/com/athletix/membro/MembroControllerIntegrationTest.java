package br.com.athletix.membro;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class MembroControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listsAndCreatesPendingMemberInsideSelectedAtletica() throws Exception {
        MvcResult csrfResult = mockMvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk()).andReturn();
        JsonNode csrf = objectMapper.readTree(csrfResult.getResponse().getContentAsString());
        MvcResult login = mockMvc.perform(post("/auth/login")
                        .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                        .header(csrf.get("headerName").asText(), csrf.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@athletix.local\",\"senha\":\"Athletix@2026\"}"))
                .andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        mockMvc.perform(get("/membros").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].email").value("admin@athletix.local"));

        mockMvc.perform(post("/membros").session(session)
                        .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                        .header(csrf.get("headerName").asText(), csrf.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"=Formula","email":"novo.membro@athletix.local","telefone":"11999999999",
                                 "curso":"Engenharia","periodoCurso":3,"cargoId":1,"departamentoId":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("=Formula"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        MvcResult exportacao = mockMvc.perform(get("/membros/exportacao").session(session))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(exportacao.getResponse().getContentAsString().contains("\"'=Formula\""));
    }

    @Test
    void exposesAdministrativeCargoAndDepartmentApisInSelectedAtletica() throws Exception {
        MvcResult csrfResult = mockMvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk()).andReturn();
        JsonNode csrf = objectMapper.readTree(csrfResult.getResponse().getContentAsString());
        MvcResult login = mockMvc.perform(post("/auth/login")
                        .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                        .header(csrf.get("headerName").asText(), csrf.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@athletix.local\",\"senha\":\"Athletix@2026\"}"))
                .andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        mockMvc.perform(get("/cargos").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber());
        mockMvc.perform(get("/departamentos").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantidadeMembros").isNumber());

        mockMvc.perform(post("/departamentos").session(session)
                        .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                        .header(csrf.get("headerName").asText(), csrf.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Comunicação\",\"descricao\":\"Novo departamento\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.situacao").value("EM_CONFIGURACAO"))
                .andExpect(jsonPath("$.quantidadeMembros").value(0));
    }
}
