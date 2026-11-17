package br.com.athletix.patrimonio.entity;

/**
 * Situação calculada a partir das movimentações. B20 possui apenas itens disponíveis; B21
 * adicionará os demais estados.
 */
public enum SituacaoOperacionalPatrimonio {
  DISPONIVEL,
  EMPRESTADO,
  EM_MANUTENCAO
}
