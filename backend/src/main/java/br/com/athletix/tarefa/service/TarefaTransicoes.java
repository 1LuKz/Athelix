package br.com.athletix.tarefa.service;

import static br.com.athletix.tarefa.entity.StatusTarefa.*;

import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.tarefa.entity.StatusTarefa;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Mesma matriz para o quadro e o checklist: concluir direto vale para quem tem permissão. */
public final class TarefaTransicoes {
  private static final Map<StatusTarefa, Set<StatusTarefa>> PERMITIDAS =
      Map.of(
          A_FAZER, EnumSet.of(EM_ANDAMENTO, CONCLUIDO),
          EM_ANDAMENTO, EnumSet.of(REVISAO, CONCLUIDO),
          REVISAO, EnumSet.of(CONCLUIDO, EM_ANDAMENTO),
          CONCLUIDO, EnumSet.of(EM_ANDAMENTO));

  private TarefaTransicoes() {}

  public static void validar(StatusTarefa atual, StatusTarefa novo, String motivo) {
    if (atual == novo) {
      throw new BusinessRuleException("A tarefa já está neste status.");
    }
    if (!PERMITIDAS.get(atual).contains(novo)) {
      throw new BusinessRuleException("Transição não permitida: " + atual + " → " + novo + ".");
    }
    boolean retorno = novo == EM_ANDAMENTO && (atual == REVISAO || atual == CONCLUIDO);
    if (retorno && (motivo == null || motivo.isBlank())) {
      throw new BusinessRuleException("Informe o motivo do retorno.");
    }
  }
}
