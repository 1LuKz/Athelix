package br.com.athletix.financeiro.entity;

/** Situação de consulta. VENCIDA é calculada, não persiste no lançamento. */
public enum SituacaoLancamento {
  ABERTO,
  LIQUIDADO,
  CANCELADO,
  VENCIDA
}
