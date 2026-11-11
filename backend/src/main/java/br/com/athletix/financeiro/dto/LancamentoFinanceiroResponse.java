package br.com.athletix.financeiro.dto;

import br.com.athletix.financeiro.entity.LancamentoFinanceiro;
import br.com.athletix.financeiro.entity.StatusAprovacao;
import br.com.athletix.financeiro.entity.StatusLiquidacao;
import br.com.athletix.financeiro.entity.TipoLancamentoFinanceiro;
import br.com.athletix.shared.api.dto.ResponseDto;
import br.com.athletix.shared.money.MoneyValues;

import java.time.Instant;
import java.time.LocalDate;

public record LancamentoFinanceiroResponse(
        Long id, Long atleticaId, Long gestaoOrigemId, Long eventoId,
        TipoLancamentoFinanceiro tipo, String descricao, Long categoriaId, String categoriaNome, String valor,
        LocalDate dataCompetencia, LocalDate dataVencimento, LocalDate dataLiquidacao,
        StatusLiquidacao statusLiquidacao, StatusAprovacao statusAprovacao, boolean vencido,
        Long criadoPorMembroId, Long aprovadoPorMembroId, Instant aprovadoEm, String motivoDecisao,
        Long liquidadoPorMembroId, Instant createdAt, Instant updatedAt, Long version
) implements ResponseDto {
    public static LancamentoFinanceiroResponse from(LancamentoFinanceiro lancamento, LocalDate hoje) {
        return new LancamentoFinanceiroResponse(lancamento.getId(), lancamento.getAtleticaId(),
                lancamento.getGestaoOrigemId(), lancamento.getEventoId(), lancamento.getTipo(),
                lancamento.getDescricao(), lancamento.getCategoriaId(), lancamento.getCategoriaNome(),
                MoneyValues.format(lancamento.getValor()), lancamento.getDataCompetencia(),
                lancamento.getDataVencimento(), lancamento.getDataLiquidacao(), lancamento.getStatusLiquidacao(),
                lancamento.getStatusAprovacao(), lancamento.vencidoEm(hoje), lancamento.getCriadoPorMembroId(),
                lancamento.getAprovadoPorMembroId(), lancamento.getAprovadoEm(), lancamento.getMotivoDecisao(),
                lancamento.getLiquidadoPorMembroId(), lancamento.getCreatedAt(), lancamento.getUpdatedAt(),
                lancamento.getVersion());
    }
}
