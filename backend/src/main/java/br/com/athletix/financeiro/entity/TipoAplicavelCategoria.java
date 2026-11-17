package br.com.athletix.financeiro.entity;

public enum TipoAplicavelCategoria {
  RECEITA,
  DESPESA,
  AMBOS;

  public boolean aceita(TipoLancamentoFinanceiro tipo) {
    return this == AMBOS || name().equals(tipo.name());
  }
}
