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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class FinanceiroB17ControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void decideLiquidaEstornaCancelaECalculaResumoEmRegimeDeCaixa() throws Exception {
        mockMvc.perform(get("/financeiro/resumo")).andExpect(status().isUnauthorized());
        Sessao admin = autenticar("admin@athletix.local", "Athletix@2026");
        Sessao financeiro = autenticar("financeiro@athletix.local", "Financeiro@2026");
        enviar(admin, get("/financeiro/evolucao-mensal"), 400, "");
        long categoriaDespesa = categoria(admin, "Esportivo");
        long categoriaReceita = categoria(admin, "Patrocínio");

        JsonNode despesa = enviar(admin, post("/financeiro/lancamentos"), 201, """
                {"tipo":"DESPESA","descricao":"Material para estorno","categoriaId":%d,
                 "valor":"40.00","dataCompetencia":"2027-01-01","dataVencimento":"2027-01-10"}
                """.formatted(categoriaDespesa));
        long despesaId = despesa.get("id").asLong();
        long versaoCriacao = despesa.get("version").asLong();
        enviar(admin, patch("/financeiro/lancamentos/{id}/aprovacao", despesaId), 400,
                "{\"decisao\":\"APROVAR\",\"motivo\":\"Tentativa própria\",\"version\":%d}".formatted(versaoCriacao));

        JsonNode aprovada = enviar(financeiro, patch("/financeiro/lancamentos/{id}/aprovacao", despesaId), 200,
                "{\"decisao\":\"APROVAR\",\"motivo\":\"Orçamento conferido\",\"version\":%d}".formatted(versaoCriacao));
        assertEquals("APROVADA", aprovada.get("statusAprovacao").asText());

        JsonNode liquidada = enviar(financeiro, post("/financeiro/lancamentos/{id}/liquidacao", despesaId), 200,
                "{\"dataLiquidacao\":\"2027-01-15\",\"version\":%d}".formatted(aprovada.get("version").asLong()));
        assertEquals("LIQUIDADO", liquidada.get("statusLiquidacao").asText());
        enviar(financeiro, post("/financeiro/lancamentos/{id}/liquidacao", despesaId), 409,
                "{\"dataLiquidacao\":\"2027-01-15\",\"version\":%d}".formatted(aprovada.get("version").asLong()));

        JsonNode estorno = enviar(admin, post("/financeiro/lancamentos/{id}/estorno", despesaId), 201,
                "{\"dataEstorno\":\"2027-01-20\",\"motivo\":\"Fornecedor devolveu o valor\"}");
        assertEquals("40.00", estorno.get("valor").asText());
        enviar(admin, post("/financeiro/lancamentos/{id}/estorno", despesaId), 409,
                "{\"dataEstorno\":\"2027-01-20\",\"motivo\":\"Repetição\"}");

        JsonNode rejeitada = enviar(admin, post("/financeiro/lancamentos"), 201, """
                {"tipo":"DESPESA","descricao":"Conta rejeitada","categoriaId":%d,
                 "valor":"25.00","dataCompetencia":"2027-01-01","dataVencimento":"2027-01-10"}
                """.formatted(categoriaDespesa));
        JsonNode decisaoRejeitada = enviar(financeiro,
                patch("/financeiro/lancamentos/{id}/aprovacao", rejeitada.get("id").asLong()), 200,
                "{\"decisao\":\"REJEITAR\",\"motivo\":\"Sem comprovante\",\"version\":%d}"
                        .formatted(rejeitada.get("version").asLong()));
        enviar(financeiro, post("/financeiro/lancamentos/{id}/liquidacao", rejeitada.get("id").asLong()), 400,
                "{\"dataLiquidacao\":\"2027-01-15\",\"version\":%d}"
                        .formatted(decisaoRejeitada.get("version").asLong()));

        JsonNode receitaCancelada = enviar(admin, post("/financeiro/lancamentos"), 201, """
                {"tipo":"RECEITA","descricao":"Receita cancelada","categoriaId":%d,
                 "valor":"30.00","dataCompetencia":"2027-01-01","dataVencimento":"2027-01-10"}
                """.formatted(categoriaReceita));
        JsonNode cancelada = enviar(financeiro,
                patch("/financeiro/lancamentos/{id}/cancelamento", receitaCancelada.get("id").asLong()), 200,
                "{\"motivo\":\"Patrocínio não confirmado\",\"version\":%d}"
                        .formatted(receitaCancelada.get("version").asLong()));
        assertEquals("CANCELADO", cancelada.get("statusLiquidacao").asText());
        assertEquals("Patrocínio não confirmado", cancelada.get("motivoCancelamento").asText());

        JsonNode resumo = enviar(admin, get("/financeiro/resumo?dataReferencia=2027-01-31"), 200, "");
        assertEquals("40.00", resumo.get("pagamentos").asText());
        assertEquals("40.00", resumo.get("estornosDespesas").asText());
        assertEquals("0.00", resumo.get("saldoDisponivel").asText());

        JsonNode contas = enviar(admin, get("/financeiro/contas"), 200, "");
        assertEquals(0, contas.get("totalItems").asInt());

        JsonNode evolucao = enviar(admin, get("/financeiro/evolucao-mensal?mesDe=2027-01&mesAte=2027-02"), 200, "");
        assertEquals(2, evolucao.size());
        assertEquals("0.00", evolucao.get(0).get("saldo").asText());
        assertEquals("0.00", evolucao.get(1).get("receitas").asText());

        MvcResult relatorio = mockMvc.perform(get("/financeiro/relatorio?dataDe=2027-01-01&dataAte=2027-01-31")
                        .session(admin.session()).cookie(admin.csrfCookie()).header(admin.csrfHeader(), admin.csrfToken()))
                .andExpect(status().isOk()).andReturn();
        assertTrue(relatorio.getResponse().getContentAsString().contains("Saldo final"));
    }

    private long categoria(Sessao sessao, String nome) throws Exception {
        JsonNode categorias = enviar(sessao, get("/financeiro/categorias"), 200, "");
        for (JsonNode categoria : categorias) {
            if (nome.equals(categoria.get("nome").asText())) return categoria.get("id").asLong();
        }
        throw new AssertionError("Categoria não encontrada: " + nome);
    }

    private Sessao autenticar(String email, String senha) throws Exception {
        MvcResult csrfResult = mockMvc.perform(get("/auth/csrf")).andExpect(status().isOk()).andReturn();
        JsonNode csrf = json(csrfResult);
        MvcResult login = mockMvc.perform(post("/auth/login")
                        .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                        .header(csrf.get("headerName").asText(), csrf.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha)))
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
