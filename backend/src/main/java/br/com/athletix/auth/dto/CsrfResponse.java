package br.com.athletix.auth.dto;

import br.com.athletix.shared.api.dto.ResponseDto;

public record CsrfResponse(String token, String headerName, String parameterName) implements ResponseDto {
}
