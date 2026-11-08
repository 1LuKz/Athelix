package br.com.athletix.shared.error;

/** Erro de validação associado a um campo de entrada. */
public record FieldErrorResponse(String field, String message) {
}
