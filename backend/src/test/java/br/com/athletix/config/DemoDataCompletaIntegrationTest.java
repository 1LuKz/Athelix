package br.com.athletix.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "athletix.demo.completa=true")
@ActiveProfiles("dev")
class DemoDataCompletaIntegrationTest {
  @Autowired private JdbcTemplate banco;

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
                Map.entry("ANEXOS_EVENTO", 0L),
                Map.entry("ARQUIVOS", 2L),
                Map.entry("ATIVIDADES_CRONOGRAMA", 0L),
                Map.entry("ATIVIDADES_USUARIO", 1L),
                Map.entry("ATLETICAS", 2L),
                Map.entry("CARGOS", 5L),
                Map.entry("CATEGORIAS_FINANCEIRAS", 6L),
                Map.entry("CATEGORIAS_PATRIMONIO", 4L),
                Map.entry("CONFERENCIAS_INVENTARIO", 1L),
                Map.entry("DEPARTAMENTOS", 3L),
                Map.entry("DEVOLUCOES_PATRIMONIO", 0L),
                Map.entry("DOCUMENTOS", 1L),
                Map.entry("DOMINIOS_EMAIL_PERMITIDOS", 2L),
                Map.entry("EMPRESTIMOS_PATRIMONIO", 1L),
                Map.entry("ESTORNOS_FINANCEIROS", 0L),
                Map.entry("EVENTOS", 4L),
                Map.entry("GESTOES", 2L),
                Map.entry("ITENS_CONFERENCIA", 1L),
                Map.entry("ITENS_PATRIMONIO", 3L),
                Map.entry("ITENS_TRANSICAO_GESTAO", 0L),
                Map.entry("LANCAMENTOS_FINANCEIROS", 10L),
                Map.entry("MANUTENCOES_PATRIMONIO", 1L),
                Map.entry("MEMBROS", 7L),
                Map.entry("NOTIFICACOES", 1L),
                Map.entry("PARTICIPACOES_EVENTO", 0L),
                Map.entry("RESPONSABILIDADES_EVENTO", 0L),
                Map.entry("SOLICITACOES_ALTERACAO_VINCULO", 0L),
                Map.entry("TAREFAS", 4L),
                Map.entry("TOKENS_CREDENCIAL", 0L),
                Map.entry("TRANSICOES_GESTAO", 0L),
                Map.entry("USUARIOS", 7L),
                Map.entry("VERSOES_DOCUMENTO", 2L),
                Map.entry("VINCULOS_GESTAO", 5L)));
  }
}
