package br.com.athletix.tarefa;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class TarefaControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String EVENTO = """
            {"nome":"Tarefas B14","inicio":"2027-05-10T10:00:00-03:00","fim":"2027-05-11T22:00:00-03:00",
             "local":"Centro","orcamentoPrevisto":"10.00"}""";

    @Test
    void percorreQuadroChecklistEAtraso() throws Exception {
        Sessao s = autenticar();
        long eventoId = enviar(s, post("/eventos"), 201, EVENTO).get("id").asLong();

        JsonNode a = enviar(s, post("/tarefas"), 201, """
                {"eventoId":%d,"titulo":"Alugar som","prioridade":"ALTA","prazo":"2020-01-01","responsavelMembroId":1}
                """.formatted(eventoId));
        assertEquals("A_FAZER", a.get("status").asText());
        assertTrue(a.get("atrasada").asBoolean());
        enviar(s, post("/tarefas"), 201, """
                {"eventoId":%d,"titulo":"Imprimir cartazes","prioridade":"BAIXA"}""".formatted(eventoId));

        // responsável inexistente na atlética
        enviar(s, post("/tarefas"), 404, """
                {"eventoId":%d,"titulo":"X","prioridade":"ALTA","responsavelMembroId":999999}""".formatted(eventoId));

        // filtros
        assertEquals(2, enviar(s, get("/tarefas?eventoId={id}", eventoId), 200, "").get("totalItems").asInt());
        assertEquals(1, enviar(s, get("/tarefas?eventoId={id}&atrasada=true", eventoId), 200, "").get("totalItems").asInt());
        assertEquals(1, enviar(s, get("/tarefas?eventoId={id}&prioridade=BAIXA", eventoId), 200, "").get("totalItems").asInt());

        // transições
        long idA = a.get("id").asLong();
        enviar(s, patch("/tarefas/{id}/status", idA), 400, status("REVISAO", null, a));      // pulo proibido
        JsonNode andamento = enviar(s, patch("/tarefas/{id}/status", idA), 200, status("EM_ANDAMENTO", null, a));
        JsonNode revisao = enviar(s, patch("/tarefas/{id}/status", idA), 200, status("REVISAO", null, andamento));
        enviar(s, patch("/tarefas/{id}/status", idA), 400, status("EM_ANDAMENTO", null, revisao)); // sem motivo
        JsonNode reaberta = enviar(s, patch("/tarefas/{id}/status", idA), 200, status("EM_ANDAMENTO", "Faltou item", revisao));
        // versão antiga → 409
        enviar(s, patch("/tarefas/{id}/status", idA), 409, status("CONCLUIDO", null, revisao));
        JsonNode concluida = enviar(s, patch("/tarefas/{id}/status", idA), 200, status("CONCLUIDO", null, reaberta));
        assertFalse(concluida.get("atrasada").asBoolean());
        assertFalse(concluida.get("concluidaEm").isNull());

        // checklist: 1 de 2 concluídas
        JsonNode checklist = enviar(s, get("/eventos/{id}/checklist", eventoId), 200, "");
        assertEquals(2, checklist.get("total").asInt());
        assertEquals(1, checklist.get("concluidas").asInt());
        assertEquals(50, checklist.get("progresso").asInt());

        // evento sem tarefas
        long vazio = enviar(s, post("/eventos"), 201, EVENTO).get("id").asLong();
        JsonNode vazia = enviar(s, get("/eventos/{id}/checklist", vazio), 200, "");
        assertEquals(0, vazia.get("progresso").asInt());
        assertFalse(vazia.get("mensagem").asText().isBlank());
    }

    private static String status(String novo, String motivo, JsonNode tarefa) {
        return "{\"status\":\"%s\",%s\"version\":%d}".formatted(novo,
                motivo == null ? "" : "\"motivo\":\"" + motivo + "\",", tarefa.get("version").asLong());
    }

    private JsonNode enviar(Sessao s, MockHttpServletRequestBuilder req, int esperado, String corpo) throws Exception {
        MvcResult r = mockMvc.perform(req.session(s.session()).cookie(s.cookie()).header(s.header(), s.token())
                .contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn();
        String texto = r.getResponse().getContentAsString();
        return texto.isBlank() ? null : objectMapper.readTree(texto);
    }

    // + autenticar(), json() e record Sessao copiados do teste de cronograma
}