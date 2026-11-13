package br.com.athletix.evento.financeiro.entity;

/** Situação informativa: percentual não existe quando o evento não possui orçamento previsto. */
public enum SituacaoOrcamentoEvento {
    SEM_ORCAMENTO,
    DENTRO_DO_ORCAMENTO,
    ESTOURADO
}
