package br.com.athletix.gestao.dto;

import br.com.athletix.gestao.entity.Gestao;
import br.com.athletix.gestao.entity.StatusGestao;
import br.com.athletix.shared.api.dto.ResponseDto;

import java.time.Instant;
import java.time.LocalDate;

public record GestaoResponse(Long id, Long atleticaId, String nome, LocalDate dataInicio, LocalDate dataFimPrevista,
                             Instant encerradaEm, StatusGestao status, Long version, Instant createdAt,
                             Instant updatedAt) implements ResponseDto {
    public static GestaoResponse from(Gestao gestao) {
        return new GestaoResponse(gestao.getId(), gestao.getAtleticaId(), gestao.getNome(), gestao.getDataInicio(),
                gestao.getDataFimPrevista(), gestao.getEncerradaEm(), gestao.getStatus(), gestao.getVersion(),
                gestao.getCreatedAt(), gestao.getUpdatedAt());
    }
}
