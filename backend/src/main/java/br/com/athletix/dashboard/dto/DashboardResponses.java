package br.com.athletix.dashboard.dto;

import br.com.athletix.financeiro.dto.EvolucaoMensalResponse;
import br.com.athletix.financeiro.dto.FinanceiroResumoResponse;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public final class DashboardResponses {
  private DashboardResponses() {}

  public record Resumo(
      long totalMembros,
      long pendentes,
      long ativos,
      long inativos,
      long eventosAtivos,
      String saldoAtual,
      long cadastrosPatrimonio,
      long unidadesPatrimonio) {}

  public record Financeiro(
      FinanceiroResumoResponse resumo, List<EvolucaoMensalResponse> seisMeses) {}

  public record Departamento(
      Long id, String nome, long membrosAtivos, int participacaoPercentual) {}

  public record Evento(Long id, String nome, OffsetDateTime inicio, String status) {}

  public record Tarefa(Long id, String titulo, LocalDate prazo, String status, boolean atrasada) {}

  public record Movimento(String tipo, LocalDate data, String valor, String descricao) {}
}
