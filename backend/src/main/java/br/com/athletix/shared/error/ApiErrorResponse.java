package br.com.athletix.shared.error;

import br.com.athletix.shared.api.dto.ResponseDto;

import java.time.Instant;
import java.util.List;

/** Resposta de erro estável, sem detalhes internos ou stack trace. */
public record ApiErrorResponse(
        int status,
        String code,
        String message,
        List<FieldErrorResponse> fieldErrors,
        String path,
        Instant timestamp
) implements ResponseDto {
}
