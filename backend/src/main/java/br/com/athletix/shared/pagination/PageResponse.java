package br.com.athletix.shared.pagination;

import br.com.athletix.shared.api.dto.ResponseDto;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/** Formato único de respostas paginadas. */
public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages)
    implements ResponseDto {

  public static <T, R> PageResponse<R> from(Page<T> page, Function<T, R> mapper) {
    return new PageResponse<>(
        page.getContent().stream().map(mapper).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }
}
