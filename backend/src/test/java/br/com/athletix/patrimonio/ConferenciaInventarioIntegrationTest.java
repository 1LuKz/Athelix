package br.com.athletix.patrimonio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ConferenciaInventarioIntegrationTest {
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @Test
  void fotografaItensRegistraDivergenciaESemAjustarPatrimonio() throws Exception {
    Sessao admin = login("admin@athletix.local", "Athletix@2026");
    long categoria = id(admin, "/patrimonio/categorias", "Eletrônicos");
    long departamento = id(admin, "/departamentos", "Diretoria");
    String sufixo = Long.toUnsignedString(System.nanoTime());
    JsonNode item =
        send(
            admin,
            post("/patrimonio/itens"),
            201,
            """
            {"nome":"Lote conferência %s","categoriaId":%d,"quantidadeTotal":5,"conservacao":"BOM",
             "localizacao":"Almoxarifado","departamentoResponsavelId":%d,"valorEstimadoUnitario":"10.00","ativo":true}
            """
                .formatted(sufixo, categoria, departamento));

    JsonNode conferencia =
        send(
            admin,
            post("/patrimonio/conferencias"),
            201,
            "{\"nome\":\"Inventário %s\"}".formatted(sufixo));
    long conferenciaId = conferencia.get("id").asLong();
    JsonNode detalhe = send(admin, get("/patrimonio/conferencias/{id}", conferenciaId), 200, "");
    JsonNode itemConferencia = itemConferencia(detalhe, item.get("id").asLong());
    assertEquals(5, itemConferencia.get("quantidadeEsperada").asInt());

    JsonNode posterior =
        send(
            admin,
            post("/patrimonio/itens"),
            201,
            """
            {"nome":"Não fotografado %s","categoriaId":%d,"quantidadeTotal":1,"conservacao":"OTIMO",
             "localizacao":"Almoxarifado","departamentoResponsavelId":%d,"valorEstimadoUnitario":"1.00","ativo":true}
            """
                .formatted(sufixo, categoria, departamento));
    JsonNode aposNovoItem =
        send(admin, get("/patrimonio/conferencias/{id}", conferenciaId), 200, "");
    assertFalse(contemItem(aposNovoItem, posterior.get("id").asLong()));

    send(
        admin,
        put("/patrimonio/conferencias/{id}/itens/{itemId}", conferenciaId, item.get("id").asLong()),
        400,
        "{\"quantidadeVerificada\":4,\"version\":%d}"
            .formatted(itemConferencia.get("version").asLong()));
    JsonNode conferido =
        send(
            admin,
            put(
                "/patrimonio/conferencias/{id}/itens/{itemId}",
                conferenciaId,
                item.get("id").asLong()),
            200,
            "{\"quantidadeVerificada\":4,\"observacao\":\"Uma unidade não localizada\",\"version\":%d}"
                .formatted(itemConferencia.get("version").asLong()));
    assertEquals(-1, conferido.get("divergencia").asInt());

    JsonNode encerrada =
        send(
            admin,
            patch("/patrimonio/conferencias/{id}/encerrar", conferenciaId),
            200,
            "{\"version\":%d}".formatted(conferencia.get("version").asLong()));
    assertEquals("ENCERRADA", encerrada.get("conferencia").get("status").asText());
    assertEquals(5, getItem(admin, item.get("id").asLong()).get("quantidadeTotal").asInt());
    send(
        admin,
        put("/patrimonio/conferencias/{id}/itens/{itemId}", conferenciaId, item.get("id").asLong()),
        400,
        "{\"quantidadeVerificada\":5,\"version\":%d}".formatted(conferido.get("version").asLong()));
  }

  private JsonNode itemConferencia(JsonNode detalhe, long itemId) {
    for (JsonNode item : detalhe.get("itens"))
      if (item.get("itemId").asLong() == itemId) return item;
    throw new AssertionError("Item fotografado não encontrado");
  }

  private boolean contemItem(JsonNode detalhe, long itemId) {
    for (JsonNode item : detalhe.get("itens"))
      if (item.get("itemId").asLong() == itemId) return true;
    return false;
  }

  private JsonNode getItem(Sessao s, long id) throws Exception {
    return send(s, get("/patrimonio/itens/{id}", id), 200, "");
  }

  private long id(Sessao s, String rota, String nome) throws Exception {
    for (JsonNode n : send(s, get(rota), 200, ""))
      if (nome.equals(n.get("nome").asText())) return n.get("id").asLong();
    throw new AssertionError("Registro inicial não encontrado: " + nome);
  }

  private Sessao login(String email, String senha) throws Exception {
    MvcResult csrf = mockMvc.perform(get("/auth/csrf")).andExpect(status().isOk()).andReturn();
    JsonNode token = json(csrf);
    MvcResult login =
        mockMvc
            .perform(
                post("/auth/login")
                    .cookie(csrf.getResponse().getCookie("XSRF-TOKEN"))
                    .header(token.get("headerName").asText(), token.get("token").asText())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha)))
            .andExpect(status().isOk())
            .andReturn();
    return new Sessao(
        (MockHttpSession) login.getRequest().getSession(false),
        csrf.getResponse().getCookie("XSRF-TOKEN"),
        token.get("headerName").asText(),
        token.get("token").asText());
  }

  private JsonNode send(
      Sessao s, MockHttpServletRequestBuilder request, int expectedStatus, String body)
      throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                request
                    .session(s.session())
                    .cookie(s.cookie())
                    .header(s.header(), s.token())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().is(expectedStatus))
            .andReturn();
    return result.getResponse().getContentAsString().isBlank() ? null : json(result);
  }

  private JsonNode json(MvcResult result) throws Exception {
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private record Sessao(
      MockHttpSession session, jakarta.servlet.http.Cookie cookie, String header, String token) {}
}
