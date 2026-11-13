package br.com.athletix.patrimonio.dto;

import br.com.athletix.patrimonio.entity.ItemPatrimonio;
import br.com.athletix.patrimonio.entity.SituacaoOperacionalPatrimonio;
import br.com.athletix.shared.money.MoneyValues;
import br.com.athletix.patrimonio.service.PatrimonioDisponibilidadeService.Disponibilidade;
import java.time.Instant;

public record ItemPatrimonioResponse(Long id, Long atleticaId, Long gestaoOrigemId, String nome, String descricao,
        Long categoriaId, String categoriaNome, int quantidadeTotal, int quantidadeDisponivel,
        br.com.athletix.patrimonio.entity.ConservacaoPatrimonio conservacao,
        SituacaoOperacionalPatrimonio situacaoOperacional, String localizacao, Long departamentoResponsavelId,
        String departamentoResponsavelNome, String valorEstimadoUnitario, boolean ativo, Instant createdAt,
        Instant updatedAt, Long version) {
    public static ItemPatrimonioResponse from(ItemPatrimonio item, Disponibilidade disponibilidade) {
        return new ItemPatrimonioResponse(item.getId(), item.getAtleticaId(), item.getGestaoOrigemId(), item.getNome(),
                item.getDescricao(), item.getCategoriaId(), item.getCategoriaNome(), item.getQuantidadeTotal(),
                disponibilidade.quantidadeDisponivel(), item.getConservacao(), situacao(disponibilidade),
                item.getLocalizacao(), item.getDepartamentoResponsavelId(), item.getDepartamentoResponsavelNome(),
                MoneyValues.format(item.getValorEstimadoUnitario()), item.isAtivo(), item.getCreatedAt(),
                item.getUpdatedAt(), item.getVersion());
    }
    private static SituacaoOperacionalPatrimonio situacao(Disponibilidade disponibilidade) {
        if (disponibilidade.quantidadeEmprestada() > 0) return SituacaoOperacionalPatrimonio.EMPRESTADO;
        if (disponibilidade.quantidadeEmManutencao() > 0) return SituacaoOperacionalPatrimonio.EM_MANUTENCAO;
        return SituacaoOperacionalPatrimonio.DISPONIVEL;
    }
}
