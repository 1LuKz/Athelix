package br.com.athletix.evento.dto;

import br.com.athletix.evento.entity.StatusEvento;
import br.com.athletix.shared.api.dto.ResponseDto;
import org.springframework.data.domain.Page;

import java.util.Map;

/** Página padrão acrescida das contagens para abas, calculadas sem o filtro da própria aba. */
public record EventoPageResponse(
        java.util.List<EventoResponse> items,
        int page,
        int size,
        long totalItems,
        int totalPages,
        Map<StatusEvento, Long> contagensPorStatus,
        long totalAtivos
) implements ResponseDto {
    public static EventoPageResponse from(Page<br.com.athletix.evento.entity.Evento> page,
                                           Map<StatusEvento, Long> contagensPorStatus, long totalAtivos) {
        return new EventoPageResponse(page.getContent().stream().map(EventoResponse::from).toList(), page.getNumber(),
                page.getSize(), page.getTotalElements(), page.getTotalPages(), contagensPorStatus, totalAtivos);
    }
}
