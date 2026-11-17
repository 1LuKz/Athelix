package br.com.athletix.tarefa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class TarefaControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void criaFiltraMoveEExibeChecklistPelaApiProtegida() throws Exception {
    mockMvc.perform(get("/tarefas")).andExpect(status().isUnauthorized());
    Sessao sessao = autenticar();
    long eventoId =
        enviar(
                sessao,
                post("/eventos"),
                201,
                """
                {"nome":"Tarefas B14","inicio":"2027-05-10T10:00:00-03:00","fim":"2027-05-11T22:00:00-03:00",
                 "local":"Centro","orcamentoPrevisto":"10.00"}
                """)
            .get("id")
            .asLong();

    JsonNode atrasada =
        enviar(
            sessao,
            post("/tarefas"),
            201,
            """
            {"eventoId":%d,"titulo":"Prestar contas","prioridade":"ALTA","prazo":"2020-01-01"}
            """
                .formatted(eventoId));
    enviar(
        sessao,
        post("/tarefas"),
        201,
        """
        {"eventoId":%d,"titulo":"Montar credenciais","prioridade":"BAIXA"}
        """
            .formatted(eventoId));

    assertEquals("A_FAZER", atrasada.get("status").asText());
    assertTrue(atrasada.get("atrasada").asBoolean());
    assertEquals(
        1,
        enviar(sessao, get("/tarefas?eventoId={id}&atrasada=true", eventoId), 200, "")
            .get("totalItems")
            .asInt());

    JsonNode emAndamento =
        enviar(
            sessao,
            patch("/tarefas/{id}/status", atrasada.get("id").asLong()),
            200,
            "{\"status\":\"EM_ANDAMENTO\",\"version\":" + atrasada.get("version").asLong() + "}");
    enviar(
        sessao,
        patch("/tarefas/{id}/status", atrasada.get("id").asLong()),
        409,
        "{\"status\":\"CONCLUIDO\",\"version\":" + atrasada.get("version").asLong() + "}");
    JsonNode concluida =
        enviar(
            sessao,
            patch("/tarefas/{id}/status", atrasada.get("id").asLong()),
            200,
            "{\"status\":\"CONCLUIDO\",\"version\":" + emAndamento.get("version").asLong() + "}");
    assertFalse(concluida.get("atrasada").asBoolean());

    JsonNode checklist = enviar(sessao, get("/eventos/{id}/checklist", eventoId), 200, "");
    assertEquals(2, checklist.get("total").asInt());
    assertEquals(1, checklist.get("concluidas").asInt());
    assertEquals(50, checklist.get("progresso").asInt());
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

  private JsonNode enviar(
      Sessao sessao,
      org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
      int statusEsperado,
      String corpo)
      throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                request
                    .session(sessao.session())
                    .cookie(sessao.csrfCookie())
                    .header(sessao.csrfHeader(), sessao.csrfToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(corpo))
            .andExpect(status().is(statusEsperado))
            .andReturn();
    String resposta = result.getResponse().getContentAsString();
    return resposta.isBlank() ? null : objectMapper.readTree(resposta);
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
