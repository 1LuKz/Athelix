package br.com.athletix.auth.dto;

import br.com.athletix.shared.api.dto.ResponseDto;

public record MensagemResponse(String message) implements ResponseDto {
}
