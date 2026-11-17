package br.com.athletix.gestao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.athletix.gestao.entity.StatusGestao;
import br.com.athletix.gestao.repository.GestaoRepository;
import br.com.athletix.gestao.repository.VinculoGestaoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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
class GestaoControllerIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private GestaoRepository gestaoRepository;
  @Autowired private VinculoGestaoRepository vinculoGestaoRepository;

  @Test
  void listsInitialManagementAndCreatesOnlyAValidPlannedManagement() throws Exception {
    Sessao sessao = autenticar();

    mockMvc
        .perform(get("/gestoes").session(sessao.session()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").isNumber());
    assertTrue(gestaoRepository.findByAtletica_IdAndStatus(1L, StatusGestao.ATIVA).isPresent());

    mockMvc
        .perform(
            post("/gestoes")
                .session(sessao.session())
                .cookie(sessao.csrfCookie())
                .header(sessao.csrfHeader(), sessao.csrfToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"nome\":\"Gestão Planejada"
                        + " B10\",\"dataInicio\":\"2027-01-01\",\"dataFimPrevista\":\"2027-12-31\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("PLANEJADA"));

    mockMvc
        .perform(
            post("/gestoes")
                .session(sessao.session())
                .cookie(sessao.csrfCookie())
                .header(sessao.csrfHeader(), sessao.csrfToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"nome\":\"Período"
                        + " inválido\",\"dataInicio\":\"2027-12-31\",\"dataFimPrevista\":\"2027-01-01\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void opensANewHistoricalLinkPeriodWhenCargoChanges() throws Exception {
    Sessao sessao = autenticar();
    JsonNode perfil =
        body(
            mockMvc
                .perform(get("/perfil").session(sessao.session()))
                .andExpect(status().isOk())
                .andReturn());
    long membroId = perfil.get("membroId").asLong();
    long gestaoId =
        gestaoRepository.findByAtletica_IdAndStatus(1L, StatusGestao.ATIVA).orElseThrow().getId();
    int periodosAntes =
        vinculoGestaoRepository
            .findAllByMembro_IdAndGestao_IdOrderByInicioVigenciaAsc(membroId, gestaoId)
            .size();

    JsonNode cargo =
        body(
            mockMvc
                .perform(
                    post("/cargos")
                        .session(sessao.session())
                        .cookie(sessao.csrfCookie())
                        .header(sessao.csrfHeader(), sessao.csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            "{\"nome\":\"Cargo Histórico"
                                + " B10\",\"perfilAcesso\":\"ADMINISTRADOR\",\"ativo\":true}"))
                .andExpect(status().isCreated())
                .andReturn());

    mockMvc
        .perform(
            put("/membros/{id}", membroId)
                .session(sessao.session())
                .cookie(sessao.csrfCookie())
                .header(sessao.csrfHeader(), sessao.csrfToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"curso":"%s","periodoCurso":1,"cargoId":%d,"departamentoId":%d,"version":%d}
                    """
                        .formatted(
                            perfil.get("curso").asText(),
                            cargo.get("id").asLong(),
                            perfil.get("departamentoId").asLong(),
                            perfil.get("membroVersion").asLong())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cargoId").value(cargo.get("id").asLong()));

    List<br.com.athletix.gestao.entity.VinculoGestao> periodos =
        vinculoGestaoRepository.findAllByMembro_IdAndGestao_IdOrderByInicioVigenciaAsc(
            membroId, gestaoId);
    assertEquals(periodosAntes + 1, periodos.size());
    assertNotNull(periodos.get(periodos.size() - 2).getFimVigencia());
    assertEquals(cargo.get("id").asLong(), periodos.get(periodos.size() - 1).getCargoId());
    assertTrue(periodos.get(periodos.size() - 1).getFimVigencia() == null);
  }

  private Sessao autenticar() throws Exception {
    MvcResult csrfResult =
        mockMvc.perform(get("/auth/csrf")).andExpect(status().isOk()).andReturn();
    JsonNode csrf = body(csrfResult);
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

  private JsonNode body(MvcResult result) throws Exception {
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private record Sessao(
      MockHttpSession session,
      jakarta.servlet.http.Cookie csrfCookie,
      String csrfHeader,
      String csrfToken) {}
}
