package br.com.athletix.patrimonio.dto;

/** Resumo apenas de itens ativos; disponibilidade é derivada e não armazenada no item. */
public record PatrimonioResumoResponse(long itensAtivos, long quantidadeTotal, long quantidadeDisponivel,
                                      long quantidadeEmprestada, long quantidadeEmManutencao,
                                      String valorEstimadoTotal) { }
