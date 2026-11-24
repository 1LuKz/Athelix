package br.com.athletix.shared.pagination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "athletix.demo.completa=true")
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class OrdenacaoHttpIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  private MockHttpSession sessao;

  @BeforeEach
  void autenticarAdministrador() throws Exception {
    MvcResult resultadoCsrf =
        mockMvc.perform(get("/auth/csrf")).andExpect(status().isOk()).andReturn();
    JsonNode csrf = json(resultadoCsrf);
    MvcResult login =
        mockMvc
            .perform(
                post("/auth/login")
                    .cookie(resultadoCsrf.getResponse().getCookie("XSRF-TOKEN"))
                    .header(csrf.path("headerName").asText(), csrf.path("token").asText())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"admin@athletix.local\",\"senha\":\"Athletix@2026\"}"))
            .andExpect(status().isOk())
            .andReturn();
    sessao = (MockHttpSession) login.getRequest().getSession(false);
  }

  static Stream<CasoOrdenacao> ordenacoesDoFrontend() {
    return Stream.of(
        new CasoOrdenacao(
            "/notificacoes?apenasNaoLidas=false&page=0&size=20&sort=criadaEm,desc",
            "criadaEm",
            true),
        new CasoOrdenacao(
            "/perfil/atividades?page=0&size=12&sort=ocorridaEm,desc", "ocorridaEm", true),
        new CasoOrdenacao(
            "/financeiro/lancamentos?page=0&size=12&sort=dataCompetencia,desc",
            "dataCompetencia",
            true),
        new CasoOrdenacao(
            "/financeiro/contas?page=0&size=30&sort=dataVencimento,asc", "dataVencimento", false),
        new CasoOrdenacao("/patrimonio/itens?page=0&size=12&sort=nome,asc", "nome", false));
  }

  @ParameterizedTest
  @MethodSource("ordenacoesDoFrontend")
  void respeitaOrdenacaoEnviadaPeloFrontendComBindingHttpReal(CasoOrdenacao caso) throws Exception {
    List<JsonNode> itens = consultarItens(caso.url());
    Comparator<JsonNode> comparador = comparadorDoCampo(caso.campo());
    if (caso.decrescente()) {
      comparador = comparador.reversed();
    }
    assertThat(itens)
        .hasSizeGreaterThan(1)
        .isSortedAccordingTo(comparador.thenComparingLong(item -> item.path("id").asLong()));
    assertThat(itens.stream().map(item -> item.path(caso.campo()).asText()).distinct().count())
        .as("A carga precisa distinguir a ordem solicitada")
        .isGreaterThan(1);
  }

  @ParameterizedTest
  @ValueSource(strings = {"campoInexistente,desc", "desc"})
  void rejeitaOrdenacaoInvalidaPeloBindingHttp(String ordenacao) throws Exception {
    MvcResult resultado =
        mockMvc
            .perform(get("/notificacoes?sort=" + ordenacao).session(sessao))
            .andExpect(status().isBadRequest())
            .andReturn();
    JsonNode erro = json(resultado);
    assertThat(erro.path("code").asText()).isEqualTo("INVALID_SORT");
    assertThat(erro.path("message").asText()).contains(ordenacao.split(",")[0]);
  }

  @Test
  void ordenaMembrosPorNomeComDirecaoNoMesmoParametro() throws Exception {
    assertThat(consultarItens("/membros?sort=nome,asc"))
        .isSortedAccordingTo(
            comparadorDoCampo("nome").thenComparingLong(item -> item.path("id").asLong()));
  }

  @Test
  void ordenaMembrosComCampoSemDirecao() throws Exception {
    assertThat(consultarItens("/membros?sort=nome"))
        .isSortedAccordingTo(
            comparadorDoCampo("nome").thenComparingLong(item -> item.path("id").asLong()));
  }

  @Test
  void preservaPrioridadeDeMultiplosParametrosSort() throws Exception {
    List<JsonNode> itens = consultarItens("/membros?sort=curso,asc&sort=nome,desc");
    Comparator<JsonNode> comparador =
        comparadorDoCampo("curso")
            .thenComparing(comparadorDoCampo("nome").reversed())
            .thenComparingLong(item -> item.path("id").asLong());
    assertThat(itens).hasSizeGreaterThan(1).isSortedAccordingTo(comparador);
  }

  private List<JsonNode> consultarItens(String url) throws Exception {
    MvcResult resultado =
        mockMvc.perform(get(url).session(sessao)).andExpect(status().isOk()).andReturn();
    List<JsonNode> itens = new ArrayList<>();
    json(resultado).path("items").forEach(itens::add);
    assertThat(itens).isNotEmpty();
    return itens;
  }

  private Comparator<JsonNode> comparadorDoCampo(String campo) {
    if (campo.equals("criadaEm") || campo.equals("ocorridaEm")) {
      return Comparator.comparing(item -> Instant.parse(item.path(campo).asText()));
    }
    return Comparator.comparing(item -> item.path(campo).asText());
  }

  private JsonNode json(MvcResult resultado) throws Exception {
    return objectMapper.readTree(resultado.getResponse().getContentAsString());
  }

  private record CasoOrdenacao(String url, String campo, boolean decrescente) {}
}
