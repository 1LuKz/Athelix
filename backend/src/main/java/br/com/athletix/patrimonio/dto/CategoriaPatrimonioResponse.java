package br.com.athletix.patrimonio.dto;

import br.com.athletix.patrimonio.entity.CategoriaPatrimonio;
import java.time.Instant;

public record CategoriaPatrimonioResponse(Long id, Long atleticaId, String nome, boolean ativa,
                                          Instant createdAt, Instant updatedAt, Long version) {
    public static CategoriaPatrimonioResponse from(CategoriaPatrimonio categoria) {
        return new CategoriaPatrimonioResponse(categoria.getId(), categoria.getAtleticaId(), categoria.getNome(),
                categoria.isAtiva(), categoria.getCreatedAt(), categoria.getUpdatedAt(), categoria.getVersion());
    }
}
