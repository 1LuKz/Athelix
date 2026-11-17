package br.com.athletix.tarefa.service;

import static br.com.athletix.tarefa.entity.StatusTarefa.A_FAZER;
import static br.com.athletix.tarefa.entity.StatusTarefa.CONCLUIDO;
import static br.com.athletix.tarefa.entity.StatusTarefa.EM_ANDAMENTO;
import static br.com.athletix.tarefa.entity.StatusTarefa.REVISAO;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.athletix.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

class TarefaTransicoesTest {
  @Test
  void permiteFluxoNormalEConclusaoDireta() {
    TarefaTransicoes.validar(A_FAZER, EM_ANDAMENTO, null);
    TarefaTransicoes.validar(EM_ANDAMENTO, REVISAO, null);
    TarefaTransicoes.validar(REVISAO, CONCLUIDO, null);
    TarefaTransicoes.validar(A_FAZER, CONCLUIDO, null);
  }

  @Test
  void retornoExigeMotivo() {
    assertThrows(
        BusinessRuleException.class, () -> TarefaTransicoes.validar(REVISAO, EM_ANDAMENTO, null));
    assertThrows(
        BusinessRuleException.class, () -> TarefaTransicoes.validar(CONCLUIDO, EM_ANDAMENTO, " "));
    TarefaTransicoes.validar(CONCLUIDO, EM_ANDAMENTO, "Faltou item");
  }

  @Test
  void rejeitaPulosEMesmoStatus() {
    assertThrows(
        BusinessRuleException.class, () -> TarefaTransicoes.validar(A_FAZER, REVISAO, null));
    assertThrows(
        BusinessRuleException.class, () -> TarefaTransicoes.validar(CONCLUIDO, REVISAO, "x"));
    assertThrows(
        BusinessRuleException.class,
        () -> TarefaTransicoes.validar(EM_ANDAMENTO, EM_ANDAMENTO, null));
  }
}
