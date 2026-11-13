package br.com.athletix.evento.financeiro.dto;

import br.com.athletix.evento.financeiro.entity.SituacaoOrcamentoEvento;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Valores monetários são texto decimal para preservar a precisão do contrato da API. */
public record EventoFinanceiroResumoResponse(Long eventoId, LocalDate dataReferencia, String orcamentoPrevisto,
                                             String realizado, String saldoOrcamentario,
                                             BigDecimal percentualUtilizado, SituacaoOrcamentoEvento situacao,
                                             String receitasRealizadas, String despesasAbertas) {
}
