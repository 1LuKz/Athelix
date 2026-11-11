package br.com.athletix.financeiro;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class LancamentoFinanceiroControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void criaFiltraEAtualizaLancamentoComRegrasFinanceiras() throws Exception {
        mockMvc.perform(get("/financeiro/lancamentos")).andExpect(status().isUnauthorized());
        Sessao sessao = autenticar();

        JsonNode categoria = enviar(sessao, post("/financeiro/categorias"), 201,
                "{\"nome\":\"Alimentação B16\",\"tipoAplicavel\":\"DESPESA\",\"ativa\":true}");
        long categoriaId = categoria.get("id").asLong();
        enviar(sessao, post("/financeiro/categorias"), 409,
                "{\"nome\":\"  alimentação b16  \",\"tipoAplicavel\":\"DESPESA\",\"ativa\":true}");

        enviar(sessao, post("/financeiro/lancamentos"), 400, """
                {"tipo":"RECEITA","descricao":"Categoria incompatível","categoriaId":%d,
                 "valor":"10.00","dataCompetencia":"2027-01-10","dataVencimento":"2027-01-11"}
                """.formatted(categoriaId));
        enviar(sessao, post("/financeiro/lancamentos"), 400, """
                {"tipo":"DESPESA","descricao":"Sem vencimento","categoriaId":%d,
                 "valor":"10.00","dataCompetencia":"2027-01-10"}
                """.formatted(categoriaId));

        JsonNode lancamento = enviar(sessao, post("/financeiro/lancamentos"), 201, """
                {"tipo":"DESPESA","descricao":"Almoço da equipe","categoriaId":%d,
                 "valor":"12.345","dataCompetencia":"2020-01-01","dataVencimento":"2020-01-02"}
                """.formatted(categoriaId));
        assertEquals("ABERTO", lancamento.get("statusLiquidacao").asText());
        assertEquals("PENDENTE", lancamento.get("statusAprovacao").asText());
        assertEquals("12.35", lancamento.get("valor").asText());
        assertTrue(lancamento.get("vencido").asBoolean());

        JsonNode pagina = enviar(sessao, get("/financeiro/lancamentos?situacao=VENCIDA&modoData=VENCIMENTO"), 200, "");
        assertEquals(1, pagina.get("totalItems").asInt());

        JsonNode atualizado = enviar(sessao, put("/financeiro/lancamentos/{id}", lancamento.get("id").asLong()), 200, """
                {"descricao":"Almoço atualizado","categoriaId":%d,"valor":"15.00",
                 "dataCompetencia":"2020-01-01","dataVencimento":"2020-01-02","version":%d}
                """.formatted(categoriaId, lancamento.get("version").asLong()));
        assertEquals("15.00", atualizado.get("valor").asText());
        enviar(sessao, put("/financeiro/lancamentos/{id}", lancamento.get("id").asLong()), 409, """
                {"descricao":"Versão antiga","categoriaId":%d,"valor":"15.00",
                 "dataCompetencia":"2020-01-01","dataVencimento":"2020-01-02","version":%d}
                """.formatted(categoriaId, lancamento.get("version").asLong()));
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

    private JsonNode enviar(Sessao sessao, MockHttpServletRequestBuilder request, int statusEsperado, String corpo)
            throws Exception {
        MvcResult result = mockMvc.perform(request.session(sessao.session()).cookie(sessao.csrfCookie())
                        .header(sessao.csrfHeader(), sessao.csrfToken()).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().is(statusEsperado)).andReturn();
        String resposta = result.getResponse().getContentAsString();
        return resposta.isBlank() ? null : objectMapper.readTree(resposta);
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private record Sessao(MockHttpSession session, jakarta.servlet.http.Cookie csrfCookie,
                          String csrfHeader, String csrfToken) { }
}
