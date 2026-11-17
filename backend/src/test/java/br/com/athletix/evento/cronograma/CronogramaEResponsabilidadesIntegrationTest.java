package br.com.athletix.evento.cronograma;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class CronogramaEResponsabilidadesIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void managesMultiDayScheduleAndKeepsRemovedResponsibilityInHistory() throws Exception {
    Sessao sessao = autenticar();
    JsonNode evento = criarEvento(sessao);
    long eventoId = evento.get("id").asLong();

    JsonNode atividade =
        json(
            mockMvc
                .perform(
                    post("/eventos/{id}/cronograma", eventoId)
                        .session(sessao.session())
                        .cookie(sessao.cookie())
                        .header(sessao.header(), sessao.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                            {"titulo":"Abertura","inicio":"2027-05-10T18:00:00-03:00",
                             "fim":"2027-05-10T19:00:00-03:00","ordem":2}
                            """))
                .andExpect(status().isCreated())
                .andReturn());

    mockMvc
        .perform(
            post("/eventos/{id}/cronograma", eventoId)
                .session(sessao.session())
                .cookie(sessao.cookie())
                .header(sessao.header(), sessao.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"titulo":"Atividade fora do evento","inicio":"2027-05-12T18:00:00-03:00","ordem":1}
                    """))
        .andExpect(status().isBadRequest());

    mockMvc
        .perform(
            put(
                    "/eventos/{eventoId}/cronograma/{atividadeId}",
                    eventoId,
                    atividade.get("id").asLong())
                .session(sessao.session())
                .cookie(sessao.cookie())
                .header(sessao.header(), sessao.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"titulo":"Abertura atualizada","inicio":"2027-05-10T18:00:00-03:00",
                     "fim":"2027-05-10T19:00:00-03:00","status":"EM_ANDAMENTO","ordem":2,"version":%d}
                    """
                        .formatted(atividade.get("version").asLong())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));

    JsonNode responsabilidade =
        json(
            mockMvc
                .perform(
                    post("/eventos/{id}/responsaveis", eventoId)
                        .session(sessao.session())
                        .cookie(sessao.cookie())
                        .header(sessao.header(), sessao.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"membroId\":1,\"area\":\"FINANCEIRO\"}"))
                .andExpect(status().isCreated())
                .andReturn());

    mockMvc
        .perform(
            post("/eventos/{id}/responsaveis", eventoId)
                .session(sessao.session())
                .cookie(sessao.cookie())
                .header(sessao.header(), sessao.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"membroId\":1,\"area\":\"FINANCEIRO\"}"))
        .andExpect(status().isConflict());

    mockMvc
        .perform(
            delete(
                    "/eventos/{eventoId}/responsaveis/{responsabilidadeId}",
                    eventoId,
                    responsabilidade.get("id").asLong())
                .param("version", responsabilidade.get("version").asText())
                .session(sessao.session())
                .cookie(sessao.cookie())
                .header(sessao.header(), sessao.token()))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            get("/eventos/{id}/responsaveis?incluirHistorico=true", eventoId)
                .session(sessao.session()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].ativa").value(false));
  }

  private JsonNode criarEvento(Sessao sessao) throws Exception {
    return json(
        mockMvc
            .perform(
                post("/eventos")
                    .session(sessao.session())
                    .cookie(sessao.cookie())
                    .header(sessao.header(), sessao.token())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"nome":"Cronograma B13","inicio":"2027-05-10T10:00:00-03:00",
                         "fim":"2027-05-11T22:00:00-03:00","local":"Centro","orcamentoPrevisto":"10.00"}
                        """))
            .andExpect(status().isCreated())
            .andReturn());
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
      MockHttpSession session, jakarta.servlet.http.Cookie cookie, String header, String token) {}
}
