package br.com.athletix.financeiro.entity;

public enum DecisaoAprovacaoFinanceira {
    APROVAR,
    REJEITAR;

    public StatusAprovacao paraStatus() {
        return this == APROVAR ? StatusAprovacao.APROVADA : StatusAprovacao.REJEITADA;
    }
}
