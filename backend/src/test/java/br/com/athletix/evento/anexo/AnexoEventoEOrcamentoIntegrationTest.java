package br.com.athletix.evento.anexo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.athletix.arquivo.repository.ArquivoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AnexoEventoEOrcamentoIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private ArquivoRepository arquivos;

  @Test
  void vinculaArquivoProtegidoEPreservaBlobNaRemocao() throws Exception {
    Sessao admin = autenticar("admin@athletix.local", "Athletix@2026");
    JsonNode evento = criarEvento(admin, "Evento Anexo B19", "100.00");
    MvcResult resultado =
        mockMvc
            .perform(
                multipart("/eventos/{eventoId}/anexos", evento.get("id").asLong())
                    .file(
                        new MockMultipartFile(
                            "arquivo",
                            "regulamento.pdf",
                            "application/pdf",
                            "%PDF-1.7\nregulamento".getBytes()))
                    .param("titulo", "Regulamento")
                    .param("descricao", "Regras do evento")
                    .param("tipoMaterial", "REGULAMENTO")
                    .session(admin.session())
                    .cookie(admin.csrfCookie())
                    .header(admin.csrfHeader(), admin.csrfToken()))
            .andExpect(status().isCreated())
            .andReturn();
    JsonNode anexo = json(resultado);
    long arquivoId = anexo.get("arquivoId").asLong();

    mockMvc
        .perform(
            get("/eventos/{eventoId}/anexos", evento.get("id").asLong()).session(admin.session()))
        .andExpect(status().isOk());
    mockMvc
        .perform(get("/arquivos/{id}/conteudo", arquivoId).session(admin.session()))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                    "/eventos/{eventoId}/anexos/{anexoId}",
                    evento.get("id").asLong(),
                    anexo.get("id").asLong())
                .param("version", anexo.get("version").asText())
                .session(admin.session())
                .cookie(admin.csrfCookie())
                .header(admin.csrfHeader(), admin.csrfToken()))
        .andExpect(status().isNoContent());
    assertTrue(
        arquivos.findById(arquivoId).isPresent(), "A remoção do vínculo não deve apagar o BLOB.");
    mockMvc
        .perform(get("/arquivos/{id}/conteudo", arquivoId).session(admin.session()))
        .andExpect(status().isNotFound());
  }

  @Test
  void calculaOrcamentoDoEventoSemAbaterReceitaEDescontaEstorno() throws Exception {
    Sessao admin = autenticar("admin@athletix.local", "Athletix@2026");
    Sessao financeiro = autenticar("financeiro@athletix.local", "Financeiro@2026");
    JsonNode evento = criarEvento(admin, "Evento Orçamento B19", "10.00");
    long eventoId = evento.get("id").asLong();
    long categoriaDespesa = categoria(admin, "Esportivo");
    long categoriaReceita = categoria(admin, "Patrocínio");

    JsonNode despesa =
        enviar(
            admin,
            post("/financeiro/lancamentos"),
            201,
            """
            {"eventoId":%d,"tipo":"DESPESA","descricao":"Despesa realizada","categoriaId":%d,
             "valor":"40.00","dataCompetencia":"2027-05-01","dataVencimento":"2027-05-10"}
            """
                .formatted(eventoId, categoriaDespesa));
    JsonNode aprovada =
        enviar(
            financeiro,
            patch("/financeiro/lancamentos/{id}/aprovacao", despesa.get("id").asLong()),
            200,
            "{\"decisao\":\"APROVAR\",\"motivo\":\"Ok\",\"version\":%d}"
                .formatted(despesa.get("version").asLong()));
    enviar(
        financeiro,
        post("/financeiro/lancamentos/{id}/liquidacao", despesa.get("id").asLong()),
        200,
        "{\"dataLiquidacao\":\"2027-05-15\",\"version\":%d}"
            .formatted(aprovada.get("version").asLong()));

    enviar(
        admin,
        post("/financeiro/lancamentos"),
        201,
        """
        {"eventoId":%d,"tipo":"DESPESA","descricao":"Pendente","categoriaId":%d,
         "valor":"10.00","dataCompetencia":"2027-05-01","dataVencimento":"2027-05-20"}
        """
            .formatted(eventoId, categoriaDespesa));
    JsonNode receita =
        enviar(
            admin,
            post("/financeiro/lancamentos"),
            201,
            """
            {"eventoId":%d,"tipo":"RECEITA","descricao":"Patrocínio","categoriaId":%d,
             "valor":"25.00","dataCompetencia":"2027-05-01","dataVencimento":"2027-05-10"}
            """
                .formatted(eventoId, categoriaReceita));
    enviar(
        financeiro,
        post("/financeiro/lancamentos/{id}/liquidacao", receita.get("id").asLong()),
        200,
        "{\"dataLiquidacao\":\"2027-05-15\",\"version\":%d}"
            .formatted(receita.get("version").asLong()));

    JsonNode antesDoEstorno =
        enviar(
            admin,
            get("/eventos/{id}/financeiro/resumo?dataReferencia=2027-05-31", eventoId),
            200,
            "");
    assertEquals("40.00", antesDoEstorno.get("realizado").asText());
    assertEquals("-30.00", antesDoEstorno.get("saldoOrcamentario").asText());
    assertEquals(400.0, antesDoEstorno.get("percentualUtilizado").asDouble());
    assertEquals("ESTOURADO", antesDoEstorno.get("situacao").asText());
    assertEquals("25.00", antesDoEstorno.get("receitasRealizadas").asText());
    assertEquals("10.00", antesDoEstorno.get("despesasAbertas").asText());

    enviar(
        admin,
        post("/financeiro/lancamentos/{id}/estorno", despesa.get("id").asLong()),
        201,
        "{\"dataEstorno\":\"2027-05-20\",\"motivo\":\"Valor devolvido\"}");
    JsonNode depoisDoEstorno =
        enviar(
            admin,
            get("/eventos/{id}/financeiro/resumo?dataReferencia=2027-05-31", eventoId),
            200,
            "");
    assertEquals("0.00", depoisDoEstorno.get("realizado").asText());
    assertEquals("10.00", depoisDoEstorno.get("saldoOrcamentario").asText());

    JsonNode semOrcamento = criarEvento(admin, "Evento Sem Orçamento B19", "0.00");
    JsonNode resumoSemOrcamento =
        enviar(
            admin,
            get(
                "/eventos/{id}/financeiro/resumo?dataReferencia=2027-05-31",
                semOrcamento.get("id").asLong()),
            200,
            "");
    assertTrue(resumoSemOrcamento.get("percentualUtilizado").isNull());
    assertEquals("SEM_ORCAMENTO", resumoSemOrcamento.get("situacao").asText());
  }

  private JsonNode criarEvento(Sessao sessao, String nome, String orcamento) throws Exception {
    return enviar(
        sessao,
        post("/eventos"),
        201,
        """
        {"nome":"%s","inicio":"2027-05-10T10:00:00-03:00","fim":"2027-05-10T20:00:00-03:00",
         "local":"Quadra","orcamentoPrevisto":"%s"}
        """
            .formatted(nome, orcamento));
  }

  private long categoria(Sessao sessao, String nome) throws Exception {
    JsonNode categorias = enviar(sessao, get("/financeiro/categorias"), 200, "");
    for (JsonNode categoria : categorias)
      if (nome.equals(categoria.get("nome").asText())) return categoria.get("id").asLong();
    throw new AssertionError("Categoria não encontrada: " + nome);
  }

  private Sessao autenticar(String email, String senha) throws Exception {
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

  private JsonNode enviar(
      Sessao sessao, MockHttpServletRequestBuilder request, int expected, String body)
      throws Exception {
    MvcResult resultado =
        mockMvc
            .perform(
                request
                    .session(sessao.session())
                    .cookie(sessao.csrfCookie())
                    .header(sessao.csrfHeader(), sessao.csrfToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().is(expected))
            .andReturn();
    return resultado.getResponse().getContentAsString().isBlank() ? null : json(resultado);
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
