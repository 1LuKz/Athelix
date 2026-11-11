@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class NotificacaoPrazoIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private NotificacaoPrazoService prazoService;
    @Autowired private NotificacaoRepository notificacoes;
    @Autowired private MembroRepository membros;

    @Test
    void geraUmAvisoPorDiaRespeitandoFusoConclusaoELeitura() throws Exception {
        Sessao s = autenticar();
        long atleticaId = membros.findById(1L).orElseThrow().getAtleticaId();
        long eventoId = enviar(s, post("/eventos"), 201, """
                {"nome":"Aviso B15","inicio":"2027-05-10T10:00:00-03:00","fim":"2027-05-11T22:00:00-03:00",
                 "local":"Centro","orcamentoPrevisto":"10.00"}""").get("id").asLong();
        JsonNode tarefa = enviar(s, post("/tarefas"), 201, """
                {"eventoId":%d,"titulo":"Prestar contas","prioridade":"ALTA","prazo":"2027-06-11","responsavelMembroId":1}
                """.formatted(eventoId));
        long tarefaId = tarefa.get("id").asLong();

        // véspera (12:00 em São Paulo): PRAZO_PROXIMO, sem duplicar na segunda execução
        Instant vespera = Instant.parse("2027-06-10T15:00:00Z");
        prazoService.gerarParaAtletica(atleticaId, vespera);
        assertTrue(notificacoes.existsByDestinatarioMembroIdAndTarefaIdAndTipoAndDataReferencia(
                1L, tarefaId, TipoNotificacao.PRAZO_PROXIMO, LocalDate.of(2027, 6, 10)));
        assertEquals(0, prazoService.gerarParaAtletica(atleticaId, vespera));

        // 23:00 do dia do prazo em São Paulo (já é dia 12 em UTC): ainda não está atrasada
        prazoService.gerarParaAtletica(atleticaId, Instant.parse("2027-06-12T02:00:00Z"));
        assertFalse(notificacoes.existsByDestinatarioMembroIdAndTarefaIdAndTipoAndDataReferencia(
                1L, tarefaId, TipoNotificacao.ATRASADA, LocalDate.of(2027, 6, 11)));
        assertFalse(notificacoes.existsByDestinatarioMembroIdAndTarefaIdAndTipoAndDataReferencia(
                1L, tarefaId, TipoNotificacao.ATRASADA, LocalDate.of(2027, 6, 12)));

        // dia seguinte: ATRASADA
        prazoService.gerarParaAtletica(atleticaId, Instant.parse("2027-06-12T15:00:00Z"));
        assertTrue(notificacoes.existsByDestinatarioMembroIdAndTarefaIdAndTipoAndDataReferencia(
                1L, tarefaId, TipoNotificacao.ATRASADA, LocalDate.of(2027, 6, 12)));

        // leitura pela API
        JsonNode lista = enviar(s, get("/notificacoes?apenasNaoLidas=true&size=100"), 200, "");
        JsonNode item = null;
        for (JsonNode n : lista.get("items")) {
            if (n.get("tarefaId").asLong() == tarefaId && "ATRASADA".equals(n.get("tipo").asText())) item = n;
        }
        assertNotNull(item);
        assertFalse(enviar(s, patch("/notificacoes/{id}/ler", item.get("id").asLong()), 200, "").get("lidaEm").isNull());
        enviar(s, patch("/notificacoes/{id}/ler", 999999), 404, "");

        // concluída não notifica mais
        enviar(s, patch("/tarefas/{id}/status", tarefaId), 200,
                "{\"status\":\"CONCLUIDO\",\"version\":" + tarefa.get("version").asLong() + "}");
        prazoService.gerarParaAtletica(atleticaId, Instant.parse("2027-06-13T15:00:00Z"));
        assertFalse(notificacoes.existsByDestinatarioMembroIdAndTarefaIdAndTipoAndDataReferencia(
                1L, tarefaId, TipoNotificacao.ATRASADA, LocalDate.of(2027, 6, 13)));
    }
}