package br.com.athletix.shared.pagination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

/** Parâmetros de paginação recebidos pela API. A página é indexada a partir de zero. */
public record PageQuery(
        @Min(value = 0, message = "A página deve ser maior ou igual a zero.") Integer page,
        @Min(value = 1, message = "O tamanho da página deve ser maior que zero.")
        @Max(value = 100, message = "O tamanho máximo da página é 100.") Integer size,
        List<String> sort
) {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public int resolvedPage() {
        return page == null ? DEFAULT_PAGE : page;
    }

    public int resolvedSize() {
        return size == null ? DEFAULT_SIZE : size;
    }
}
