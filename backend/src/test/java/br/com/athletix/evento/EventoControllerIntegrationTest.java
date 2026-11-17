package br.com.athletix.evento;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class EventoControllerIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void createsFiltersAndTransitionsEventWithAuditedCorrection() throws Exception {
    Sessao sessao = autenticar();
    MvcResult criado =
        mockMvc
            .perform(
                post("/eventos")
                    .session(sessao.session())
                    .cookie(sessao.csrfCookie())
                    .header(sessao.csrfHeader(), sessao.csrfToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"nome":"Festival B11","descricao":"Evento gratuito","inicio":"2027-03-10T18:00:00-03:00",
                         "fim":"2027-03-10T22:00:00-03:00","local":"Quadra Central","cidade":"São Paulo",
                         "uf":"sp","orcamentoPrevisto":"0.00","participantesEsperados":300}
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PLANEJADO"))
            .andExpect(jsonPath("$.orcamentoPrevisto").value("0.00"))
            .andExpect(jsonPath("$.gestaoOrigemId").isNumber())
            .andReturn();
    JsonNode evento = json(criado);

    mockMvc
        .perform(
            get("/eventos")
                .session(sessao.session())
                .param("busca", "festival b11")
                .param("grupoStatus", "ATIVOS"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalItems").value(0))
        .andExpect(jsonPath("$.contagensPorStatus.PLANEJADO").value(1));

    evento = alterarStatus(sessao, evento, "INSCRICOES_ABERTAS", null);
    evento = alterarStatus(sessao, evento, "EM_ANDAMENTO", null);

    mockMvc
        .perform(
            patch("/eventos/{id}/status", evento.get("id").asLong())
                .session(sessao.session())
                .cookie(sessao.csrfCookie())
                .header(sessao.csrfHeader(), sessao.csrfToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"status":"INSCRICOES_ABERTAS","version":%d}
                    """
                        .formatted(evento.get("version").asLong())))
        .andExpect(status().isBadRequest());

    evento =
        alterarStatus(sessao, evento, "INSCRICOES_ABERTAS", "Inscrições precisam ser reabertas.");
    mockMvc
        .perform(
            get("/eventos")
                .session(sessao.session())
                .param("busca", "Festival B11")
                .param("grupoStatus", "ATIVOS"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].id").value(evento.get("id").asLong()))
        .andExpect(jsonPath("$.totalAtivos").value(1));
  }

  @Test
  void rejectsAnEventEndingBeforeItStarts() throws Exception {
    Sessao sessao = autenticar();
    mockMvc
        .perform(
            post("/eventos")
                .session(sessao.session())
                .cookie(sessao.csrfCookie())
                .header(sessao.csrfHeader(), sessao.csrfToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Data inválida","inicio":"2027-03-11T18:00:00-03:00",
                     "fim":"2027-03-10T22:00:00-03:00","local":"Quadra","orcamentoPrevisto":"5.00"}
                    """))
        .andExpect(status().isBadRequest());
  }

  private JsonNode alterarStatus(Sessao sessao, JsonNode evento, String statusEvento, String motivo)
      throws Exception {
    String motivoJson = motivo == null ? "" : ",\"motivoCorrecao\":\"" + motivo + "\"";
    MvcResult result =
        mockMvc
            .perform(
                patch("/eventos/{id}/status", evento.get("id").asLong())
                    .session(sessao.session())
                    .cookie(sessao.csrfCookie())
                    .header(sessao.csrfHeader(), sessao.csrfToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"status\":\"%s\",\"version\":%d%s}"
                            .formatted(statusEvento, evento.get("version").asLong(), motivoJson)))
            .andExpect(status().isOk())
            .andReturn();
    return json(result);
  }

  private Sessao autenticar() throws Exception {
    MvcResult csrfResult =
        mockMvc.perform(get("/auth/csrf")).andExpect(status().isOk()).andReturn();
    JsonNode csrf = json(csrfResult);
    MvcResult login =
        mockMvc
            .perform(
                post("/auth/login")
                    .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                    .header(csrf.get("headerName").asText(), csrf.get("token").asText())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"admin@athletix.local\",\"senha\":\"Athletix@2026\"}"))
            .andExpect(status().isOk())
            .andReturn();
    return new Sessao(
        (MockHttpSession) login.getRequest().getSession(false),
        csrfResult.getResponse().getCookie("XSRF-TOKEN"),
        csrf.get("headerName").asText(),
        csrf.get("token").asText());
  }

  private JsonNode json(MvcResult result) throws Exception {
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private record Sessao(
      MockHttpSession session,
      jakarta.servlet.http.Cookie csrfCookie,
      String csrfHeader,
      String csrfToken) {}
}
