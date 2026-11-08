package br.com.athletix.auth;

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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authenticatesDevAdministratorWithCsrfAndPersistsSession() throws Exception {
        MvcResult csrfResult = mockMvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
                .andReturn();
        JsonNode csrf = objectMapper.readTree(csrfResult.getResponse().getContentAsString());

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                        .header(csrf.get("headerName").asText(), csrf.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@athletix.local\",\"senha\":\"Athletix@2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.email").value("admin@athletix.local"))
                .andExpect(jsonPath("$.selecaoNecessaria").value(false))
                .andExpect(jsonPath("$.membroSelecionadoId").isNumber())
                .andExpect(jsonPath("$.capacidades").isArray())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        mockMvc.perform(get("/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.email").value("admin@athletix.local"));
    }
}
