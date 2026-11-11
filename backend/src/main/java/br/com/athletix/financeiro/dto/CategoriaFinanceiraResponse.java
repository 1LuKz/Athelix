package br.com.athletix.financeiro.dto;

import br.com.athletix.financeiro.entity.CategoriaFinanceira;
import br.com.athletix.financeiro.entity.TipoAplicavelCategoria;
import br.com.athletix.shared.api.dto.ResponseDto;

import java.time.Instant;

public record CategoriaFinanceiraResponse(Long id, Long atleticaId, String nome,
                                          TipoAplicavelCategoria tipoAplicavel, boolean ativa,
                                          Instant createdAt, Instant updatedAt, Long version) implements ResponseDto {
    public static CategoriaFinanceiraResponse from(CategoriaFinanceira categoria) {
        return new CategoriaFinanceiraResponse(categoria.getId(), categoria.getAtleticaId(), categoria.getNome(),
                categoria.getTipoAplicavel(), categoria.isAtiva(), categoria.getCreatedAt(), categoria.getUpdatedAt(),
                categoria.getVersion());
    }
}
