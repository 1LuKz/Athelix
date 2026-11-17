package br.com.athletix.notificacao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.notificacao.service.NotificacaoPrazoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
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
class NotificacaoControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private NotificacaoPrazoService prazoService;
  @Autowired private MembroRepository membros;

  @Test
  void geraListaResumeEMarcaNotificacaoPropriaComoLida() throws Exception {
    mockMvc.perform(get("/notificacoes")).andExpect(status().isUnauthorized());
    Sessao sessao = autenticar();
    long membroId = sessao.membroId();
    long atleticaId = membros.findById(membroId).orElseThrow().getAtleticaId();
    long eventoId =
        enviar(
                sessao,
                post("/eventos"),
                201,
                """
                {"nome":"Avisos B15","inicio":"2027-05-10T10:00:00-03:00","fim":"2027-05-11T22:00:00-03:00",
                 "local":"Centro","orcamentoPrevisto":"10.00"}
                """)
            .get("id")
            .asLong();
    long tarefaId =
        enviar(
                sessao,
                post("/tarefas"),
                201,
                """
                {"eventoId":%d,"titulo":"Enviar relatório","prioridade":"ALTA","prazo":"2027-06-10","responsavelMembroId":%d}
                """
                    .formatted(eventoId, membroId))
            .get("id")
            .asLong();

    prazoService.gerarParaAtletica(atleticaId, Instant.parse("2027-06-11T15:00:00Z"));

    JsonNode lista = enviar(sessao, get("/notificacoes?apenasNaoLidas=true&size=100"), 200, "");
    JsonNode notificacao = null;
    for (JsonNode item : lista.get("items")) {
      if (item.get("tarefaId").asLong() == tarefaId) {
        notificacao = item;
        break;
      }
    }
    assertNotNull(notificacao);
    assertFalse(
        enviar(sessao, patch("/notificacoes/{id}/ler", notificacao.get("id").asLong()), 200, "")
            .get("lidaEm")
            .isNull());
    enviar(sessao, patch("/notificacoes/{id}/ler", 999999L), 404, "");
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
    JsonNode body = json(login);
    return new Sessao(
        (MockHttpSession) login.getRequest().getSession(false),
        csrfResult.getResponse().getCookie("XSRF-TOKEN"),
        csrf.get("headerName").asText(),
        csrf.get("token").asText(),
        body.get("membroSelecionadoId").asLong());
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
      String csrfToken,
      long membroId) {}
}
