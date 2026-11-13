package br.com.athletix.patrimonio;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class PatrimonioControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void cadastraLoteHomogeneoFiltraEResumeSemConfundirConservacaoComDisponibilidade() throws Exception {
        Sessao admin = autenticar();
        long categoriaId = obterId(admin, "/patrimonio/categorias", "Eletrônicos");
        long departamentoId = obterId(admin, "/departamentos", "Diretoria");
        JsonNode item = enviar(admin, post("/patrimonio/itens"), 201, """
                {"nome":"Notebook laboratório","descricao":"Lote homogêneo","categoriaId":%d,
                 "quantidadeTotal":4,"conservacao":"BOM","localizacao":"Armário A",
                 "departamentoResponsavelId":%d,"valorEstimadoUnitario":"250.00","ativo":true}
                """.formatted(categoriaId, departamentoId));
        assertEquals(4, item.get("quantidadeDisponivel").asInt());
        assertEquals("DISPONIVEL", item.get("situacaoOperacional").asText());

        enviar(admin, put("/patrimonio/itens/{id}", item.get("id").asLong()), 400, """
                {"nome":"Notebook laboratório","descricao":"Lote homogêneo","categoriaId":%d,
                 "quantidadeTotal":3,"conservacao":"BOM","localizacao":"Armário A",
                 "departamentoResponsavelId":%d,"valorEstimadoUnitario":"250.00","ativo":true,"version":%d}
                """.formatted(categoriaId, departamentoId, item.get("version").asLong()));
        JsonNode atualizado = enviar(admin, put("/patrimonio/itens/{id}", item.get("id").asLong()), 200, """
                {"nome":"Notebook laboratório","descricao":"Lote homogêneo","categoriaId":%d,
                 "quantidadeTotal":3,"conservacao":"BOM","localizacao":"Armário A",
                 "departamentoResponsavelId":%d,"valorEstimadoUnitario":"250.00","ativo":true,
                 "justificativaReducao":"Uma unidade foi baixada","version":%d}
                """.formatted(categoriaId, departamentoId, item.get("version").asLong()));
        assertEquals(3, atualizado.get("quantidadeTotal").asInt());

        JsonNode filtrado = enviar(admin, get("/patrimonio/itens?conservacao=BOM&situacao=DISPONIVEL"), 200, "");
        assertEquals(1, filtrado.get("totalItems").asInt());
        JsonNode emprestado = enviar(admin, get("/patrimonio/itens?situacao=EMPRESTADO"), 200, "");
        assertEquals(0, emprestado.get("totalItems").asInt());
        JsonNode resumo = enviar(admin, get("/patrimonio/resumo"), 200, "");
        assertEquals(3, resumo.get("quantidadeTotal").asInt());
        assertEquals(3, resumo.get("quantidadeDisponivel").asInt());
        assertEquals("750.00", resumo.get("valorEstimadoTotal").asText());
    }

    private long obterId(Sessao sessao, String rota, String nome) throws Exception {
        JsonNode itens = enviar(sessao, get(rota), 200, "");
        for (JsonNode atual : itens) if (nome.equals(atual.get("nome").asText())) return atual.get("id").asLong();
        throw new AssertionError("Não encontrado: " + nome);
    }
    private Sessao autenticar() throws Exception {
        MvcResult csrf = mockMvc.perform(get("/auth/csrf")).andExpect(status().isOk()).andReturn(); JsonNode token = json(csrf);
        MvcResult login = mockMvc.perform(post("/auth/login").cookie(csrf.getResponse().getCookie("XSRF-TOKEN"))
                .header(token.get("headerName").asText(), token.get("token").asText()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@athletix.local\",\"senha\":\"Athletix@2026\"}"))
                .andExpect(status().isOk()).andReturn();
        return new Sessao((MockHttpSession) login.getRequest().getSession(false), csrf.getResponse().getCookie("XSRF-TOKEN"),
                token.get("headerName").asText(), token.get("token").asText());
    }
    private JsonNode enviar(Sessao sessao, MockHttpServletRequestBuilder request, int esperado, String corpo) throws Exception {
        MvcResult resultado = mockMvc.perform(request.session(sessao.session()).cookie(sessao.csrfCookie())
                .header(sessao.csrfHeader(), sessao.csrfToken()).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().is(esperado)).andReturn();
        return resultado.getResponse().getContentAsString().isBlank() ? null : json(resultado);
    }
    private JsonNode json(MvcResult result) throws Exception { return objectMapper.readTree(result.getResponse().getContentAsString()); }
    private record Sessao(MockHttpSession session, jakarta.servlet.http.Cookie csrfCookie, String csrfHeader, String csrfToken) { }
}
