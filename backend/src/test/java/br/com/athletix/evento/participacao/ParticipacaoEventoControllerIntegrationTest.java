package br.com.athletix.evento.participacao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ParticipacaoEventoControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void preservesParticipantsAndReportsCumulativeCounts() throws Exception {
        Sessao sessao = autenticar();
        JsonNode evento = criarEvento(sessao);
        long eventoId = evento.get("id").asLong();

        JsonNode externa = json(mockMvc.perform(post("/eventos/{id}/participacoes", eventoId).session(sessao.session())
                        .cookie(sessao.csrfCookie()).header(sessao.csrfHeader(), sessao.csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomeParticipante\":\"Ana Externa\",\"emailParticipante\":\"ANA@EXEMPLO.COM\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emailParticipante").value("ana@exemplo.com"))
                .andReturn());

        mockMvc.perform(post("/eventos/{id}/participacoes", eventoId).session(sessao.session())
                        .cookie(sessao.csrfCookie()).header(sessao.csrfHeader(), sessao.csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomeParticipante\":\"Outra Ana\",\"emailParticipante\":\"ana@exemplo.com\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/eventos/{id}/participacoes", eventoId).session(sessao.session())
                        .cookie(sessao.csrfCookie()).header(sessao.csrfHeader(), sessao.csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"membroId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.membroId").value(1));

        externa = alterarStatus(sessao, eventoId, externa, "CONFIRMADO", null);
        externa = alterarStatus(sessao, eventoId, externa, "PRESENTE", null);
        mockMvc.perform(get("/eventos/{id}/participacoes/resumo", eventoId).session(sessao.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantesEsperados").value(20))
                .andExpect(jsonPath("$.inscritos").value(2))
                .andExpect(jsonPath("$.confirmados").value(1))
                .andExpect(jsonPath("$.presentes").value(1));

        externa = alterarStatus(sessao, eventoId, externa, "CONFIRMADO", "Presença marcada por engano.");
        mockMvc.perform(get("/perfil/atividades").session(sessao.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].acao").value("PRESENCA_EVENTO_CORRIGIDA"));
    }

    private JsonNode criarEvento(Sessao sessao) throws Exception {
        return json(mockMvc.perform(post("/eventos").session(sessao.session())
                        .cookie(sessao.csrfCookie()).header(sessao.csrfHeader(), sessao.csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Participações B12","inicio":"2027-04-10T18:00:00-03:00",
                                 "fim":"2027-04-10T22:00:00-03:00","local":"Ginásio","orcamentoPrevisto":"10.00",
                                 "participantesEsperados":20}
                                """))
                .andExpect(status().isCreated()).andReturn());
    }

    private JsonNode alterarStatus(Sessao sessao, long eventoId, JsonNode participacao, String statusNovo,
                                   String motivo) throws Exception {
        String motivoJson = motivo == null ? "" : ",\"motivoCorrecao\":\"" + motivo + "\"";
        return json(mockMvc.perform(patch("/eventos/{eventoId}/participacoes/{participacaoId}/status", eventoId,
                        participacao.get("id").asLong()).session(sessao.session())
                        .cookie(sessao.csrfCookie()).header(sessao.csrfHeader(), sessao.csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"%s\",\"version\":%d%s}".formatted(statusNovo,
                                participacao.get("version").asLong(), motivoJson)))
                .andExpect(status().isOk()).andReturn());
    }

    private Sessao autenticar() throws Exception {
        MvcResult csrfResult = mockMvc.perform(get("/auth/csrf")).andExpect(status().isOk()).andReturn();
        JsonNode csrf = json(csrfResult);
        MvcResult login = mockMvc.perform(post("/auth/login")
                        .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                        .header(csrf.get("headerName").asText(), csrf.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@athletix.local\",\"senha\":\"Athletix@2026\"}"))
                .andExpect(status().isOk()).andReturn();
        return new Sessao((MockHttpSession) login.getRequest().getSession(false),
                csrfResult.getResponse().getCookie("XSRF-TOKEN"), csrf.get("headerName").asText(), csrf.get("token").asText());
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private record Sessao(MockHttpSession session, jakarta.servlet.http.Cookie csrfCookie,
                          String csrfHeader, String csrfToken) { }
}
