package br.com.athletix.patrimonio.dto;

import br.com.athletix.patrimonio.entity.ItemPatrimonio;
import br.com.athletix.patrimonio.entity.SituacaoOperacionalPatrimonio;
import br.com.athletix.shared.money.MoneyValues;
import java.time.Instant;

public record ItemPatrimonioResponse(Long id, Long atleticaId, Long gestaoOrigemId, String nome, String descricao,
        Long categoriaId, String categoriaNome, int quantidadeTotal, int quantidadeDisponivel,
        br.com.athletix.patrimonio.entity.ConservacaoPatrimonio conservacao,
        SituacaoOperacionalPatrimonio situacaoOperacional, String localizacao, Long departamentoResponsavelId,
        String departamentoResponsavelNome, String valorEstimadoUnitario, boolean ativo, Instant createdAt,
        Instant updatedAt, Long version) {
    public static ItemPatrimonioResponse from(ItemPatrimonio item) {
        // B21 substituirá a disponibilidade derivada abaixo por movimentações de empréstimo/manutenção.
        return new ItemPatrimonioResponse(item.getId(), item.getAtleticaId(), item.getGestaoOrigemId(), item.getNome(),
                item.getDescricao(), item.getCategoriaId(), item.getCategoriaNome(), item.getQuantidadeTotal(),
                item.getQuantidadeTotal(), item.getConservacao(), SituacaoOperacionalPatrimonio.DISPONIVEL,
                item.getLocalizacao(), item.getDepartamentoResponsavelId(), item.getDepartamentoResponsavelNome(),
                MoneyValues.format(item.getValorEstimadoUnitario()), item.isAtivo(), item.getCreatedAt(),
                item.getUpdatedAt(), item.getVersion());
    }
}
