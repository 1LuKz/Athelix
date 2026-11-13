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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("dev")
class EmprestimoManutencaoIntegrationTest {
    @Autowired MockMvc mockMvc; @Autowired ObjectMapper objectMapper;
    @Test void controlaDisponibilidadeEntreEmprestimoDevolucaoEManutencao() throws Exception {
        Sessao admin = login("admin@athletix.local", "Athletix@2026");
        long categoria = id(admin, "/patrimonio/categorias", "Eletrônicos"); long departamento = id(admin, "/departamentos", "Diretoria");
        long responsavel = me(admin).get("membroSelecionadoId").asLong();
        JsonNode item = send(admin, post("/patrimonio/itens"), 201, """
          {"nome":"Caixa de som B21","categoriaId":%d,"quantidadeTotal":5,"conservacao":"BOM","localizacao":"Sala",
           "departamentoResponsavelId":%d,"valorEstimadoUnitario":"100.00","ativo":true}
          """.formatted(categoria, departamento));
        JsonNode emprestimo = send(admin, post("/patrimonio/emprestimos"), 201,
                "{\"itemId\":%d,\"membroResponsavelId\":%d,\"quantidade\":3}".formatted(item.get("id").asLong(), responsavel));
        assertEquals(3, emprestimo.get("saldoAberto").asInt());
        assertEquals(2, getItem(admin, item.get("id").asLong()).get("quantidadeDisponivel").asInt());
        send(admin, post("/patrimonio/emprestimos/{id}/devolucoes", emprestimo.get("id").asLong()), 201, """
          {"quantidade":1,"condicaoRetorno":"RUIM","enviarParaManutencao":true,"motivoManutencao":"Dano no cabo"}
          """);
        JsonNode aposDevolucao = getItem(admin, item.get("id").asLong());
        assertEquals(2, aposDevolucao.get("quantidadeDisponivel").asInt());
        assertEquals("EMPRESTADO", aposDevolucao.get("situacaoOperacional").asText());
        JsonNode manutencao = send(admin, get("/patrimonio/manutencoes"), 200, "").get(0);
        send(admin, put("/patrimonio/itens/{id}", item.get("id").asLong()), 400, atualizar(item, categoria, departamento, false));
        send(admin, patch("/patrimonio/manutencoes/{id}/concluir", manutencao.get("id").asLong()), 200,
                "{\"resultado\":\"Cabo substituído\",\"version\":%d}".formatted(manutencao.get("version").asLong()));
        assertEquals(3, getItem(admin, item.get("id").asLong()).get("quantidadeDisponivel").asInt());
        send(admin, post("/patrimonio/emprestimos/{id}/devolucoes", emprestimo.get("id").asLong()), 201,
                "{\"quantidade\":2,\"condicaoRetorno\":\"BOM\"}");
        JsonNode atual = getItem(admin, item.get("id").asLong());
        send(admin, put("/patrimonio/itens/{id}", item.get("id").asLong()), 200,
                atualizar(atual, categoria, departamento, false));
    }
    private String atualizar(JsonNode item, long categoria, long departamento, boolean ativo) { return """
      {"nome":"Caixa de som B21","categoriaId":%d,"quantidadeTotal":5,"conservacao":"BOM","localizacao":"Sala",
       "departamentoResponsavelId":%d,"valorEstimadoUnitario":"100.00","ativo":%s,"version":%d}
      """.formatted(categoria, departamento, ativo, item.get("version").asLong()); }
    private JsonNode getItem(Sessao s, long id) throws Exception { return send(s, get("/patrimonio/itens/{id}", id), 200, ""); }
    private JsonNode me(Sessao s) throws Exception { return send(s, get("/auth/me"), 200, ""); }
    private long id(Sessao s, String rota, String nome) throws Exception { for (JsonNode n : send(s, get(rota), 200, "")) if (nome.equals(n.get("nome").asText())) return n.get("id").asLong(); throw new AssertionError(); }
    private Sessao login(String email, String senha) throws Exception { MvcResult c = mockMvc.perform(get("/auth/csrf")).andExpect(status().isOk()).andReturn(); JsonNode t = json(c);
        MvcResult l = mockMvc.perform(post("/auth/login").cookie(c.getResponse().getCookie("XSRF-TOKEN")).header(t.get("headerName").asText(), t.get("token").asText()).contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha))).andExpect(status().isOk()).andReturn(); return new Sessao((MockHttpSession)l.getRequest().getSession(false),c.getResponse().getCookie("XSRF-TOKEN"),t.get("headerName").asText(),t.get("token").asText()); }
    private JsonNode send(Sessao s, MockHttpServletRequestBuilder r, int code, String body) throws Exception { MvcResult x=mockMvc.perform(r.session(s.session()).cookie(s.cookie()).header(s.header(),s.token()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().is(code)).andReturn(); return x.getResponse().getContentAsString().isBlank()?null:json(x); }
    private JsonNode json(MvcResult x) throws Exception { return objectMapper.readTree(x.getResponse().getContentAsString()); }
    private record Sessao(MockHttpSession session, jakarta.servlet.http.Cookie cookie, String header, String token) { }
}
