package br.com.athletix.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "athletix.demo.completa=true")
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class DemoDataCompletaIntegrationTest {
  @Autowired private JdbcTemplate banco;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void registraContagensDaCargaCompleta() {
    Map<String, Long> contagens = new TreeMap<>();
    banco
        .queryForList(
            "select table_name from information_schema.tables "
                + "where table_schema = 'PUBLIC' and table_type = 'BASE TABLE'",
            String.class)
        .forEach(
            tabela ->
                contagens.put(
                    tabela,
                    banco.queryForObject("select count(*) from \"" + tabela + "\"", Long.class)));
    assertThat(contagens)
        .containsExactlyInAnyOrderEntriesOf(
            Map.ofEntries(
                Map.entry("ANEXOS_EVENTO", 2L),
                Map.entry("ARQUIVOS", 9L),
                Map.entry("ATIVIDADES_CRONOGRAMA", 6L),
                Map.entry("ATIVIDADES_USUARIO", 150L),
                Map.entry("ATLETICAS", 2L),
                Map.entry("CARGOS", 5L),
                Map.entry("CATEGORIAS_FINANCEIRAS", 6L),
                Map.entry("CATEGORIAS_PATRIMONIO", 4L),
                Map.entry("CONFERENCIAS_INVENTARIO", 1L),
                Map.entry("DEPARTAMENTOS", 3L),
                Map.entry("DEVOLUCOES_PATRIMONIO", 1L),
                Map.entry("DOCUMENTOS", 5L),
                Map.entry("DOMINIOS_EMAIL_PERMITIDOS", 2L),
                Map.entry("EMPRESTIMOS_PATRIMONIO", 2L),
                Map.entry("ESTORNOS_FINANCEIROS", 1L),
                Map.entry("EVENTOS", 4L),
                Map.entry("GESTOES", 2L),
                Map.entry("ITENS_CONFERENCIA", 8L),
                Map.entry("ITENS_PATRIMONIO", 8L),
                Map.entry("ITENS_TRANSICAO_GESTAO", 0L),
                Map.entry("LANCAMENTOS_FINANCEIROS", 12L),
                Map.entry("MANUTENCOES_PATRIMONIO", 1L),
                Map.entry("MEMBROS", 7L),
                Map.entry("NOTIFICACOES", 5L),
                Map.entry("PARTICIPACOES_EVENTO", 14L),
                Map.entry("RESPONSABILIDADES_EVENTO", 5L),
                Map.entry("SOLICITACOES_ALTERACAO_VINCULO", 0L),
                Map.entry("TAREFAS", 22L),
                Map.entry("TOKENS_CREDENCIAL", 0L),
                Map.entry("TRANSICOES_GESTAO", 0L),
                Map.entry("USUARIOS", 7L),
                Map.entry("VERSOES_DOCUMENTO", 7L),
                Map.entry("VINCULOS_GESTAO", 5L)));
  }

  @Test
  void administradorConsultaTodasAsAreasComConteudo() throws Exception {
    MockHttpSession sessao = autenticarAdministrador();
    Long eventoId = eventoEmAndamento();
    List<String> endpoints =
        List.of(
            "/eventos",
            "/tarefas",
            "/documentos",
            "/patrimonio/itens",
            "/patrimonio/emprestimos",
            "/patrimonio/manutencoes",
            "/patrimonio/conferencias",
            "/financeiro/lancamentos",
            "/notificacoes",
            "/perfil/atividades",
            "/eventos/" + eventoId + "/cronograma",
            "/eventos/" + eventoId + "/responsaveis",
            "/eventos/" + eventoId + "/participacoes",
            "/eventos/" + eventoId + "/anexos",
            "/dashboard/proximos-eventos",
            "/dashboard/tarefas-pendentes",
            "/dashboard/ultimas-movimentacoes");
    for (String endpoint : endpoints) {
      JsonNode resposta = consultar(endpoint, sessao);
      JsonNode itens = resposta.isArray() ? resposta : resposta.path("items");
      assertThat(itens.isArray()).as(endpoint).isTrue();
      assertThat(itens.size()).as(endpoint).isPositive();
    }
    JsonNode resumo = consultar("/dashboard/resumo", sessao);
    assertThat(resumo.path("totalMembros").asLong()).isPositive();
    assertThat(resumo.path("cadastrosPatrimonio").asLong()).isEqualTo(8);
    JsonNode checklist = consultar("/eventos/" + eventoId + "/checklist", sessao);
    assertThat(checklist.path("total").asInt()).isEqualTo(18);
    assertThat(checklist.path("concluidas").asInt()).isEqualTo(14);
  }

  @Test
  void preservaIsolamentoGestaoEVariedadeDoCenario() {
    for (String tabela :
        List.of(
            "EVENTOS",
            "TAREFAS",
            "DOCUMENTOS",
            "ITENS_PATRIMONIO",
            "LANCAMENTOS_FINANCEIROS",
            "NOTIFICACOES",
            "ATIVIDADES_USUARIO")) {
      assertThat(
              banco.queryForObject(
                  "select count(*) from "
                      + tabela
                      + " where atletica_id = (select id from atleticas where sigla = 'AAS')",
                  Long.class))
          .as(tabela + " da segunda atlética")
          .isZero();
    }
    assertThat(banco.queryForObject("select count(*) from transicoes_gestao", Long.class)).isZero();
    assertThat(banco.queryForList("select distinct status from tarefas", String.class))
        .containsExactlyInAnyOrder("A_FAZER", "EM_ANDAMENTO", "REVISAO", "CONCLUIDO");
    assertThat(banco.queryForList("select distinct categoria from documentos", String.class))
        .containsExactlyInAnyOrder("ESTATUTO", "REGIMENTO", "ATA", "CONTRATO", "OUTRO");
    assertThat(
            banco.queryForObject(
                "select count(*) from (select documento_id from versoes_documento"
                    + " group by documento_id having count(*) = 2)",
                Long.class))
        .isEqualTo(2);
    assertThat(
            banco.queryForObject(
                "select count(distinct formatdatetime(data_competencia, 'yyyy-MM'))"
                    + " from lancamentos_financeiros",
                Long.class))
        .isEqualTo(6);
  }

  private Long eventoEmAndamento() {
    return banco.queryForObject("select id from eventos where status = 'EM_ANDAMENTO'", Long.class);
  }

  private MockHttpSession autenticarAdministrador() throws Exception {
    MvcResult resultadoCsrf =
        mockMvc.perform(get("/auth/csrf")).andExpect(status().isOk()).andReturn();
    JsonNode csrf = objectMapper.readTree(resultadoCsrf.getResponse().getContentAsString());
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
    return (MockHttpSession) login.getRequest().getSession(false);
  }

  private JsonNode consultar(String endpoint, MockHttpSession sessao) throws Exception {
    MvcResult resultado = mockMvc.perform(get(endpoint).session(sessao)).andReturn();
    assertThat(resultado.getResolvedException()).as(endpoint).isNull();
    assertThat(resultado.getResponse().getStatus()).as(endpoint).isEqualTo(200);
    return objectMapper.readTree(resultado.getResponse().getContentAsString());
  }
}
