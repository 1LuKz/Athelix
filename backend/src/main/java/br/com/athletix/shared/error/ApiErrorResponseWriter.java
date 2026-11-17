package br.com.athletix.shared.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/** Escreve erros produzidos antes de um controller, como falhas no filtro de segurança. */
@Component
public class ApiErrorResponseWriter {

  private final ObjectMapper objectMapper;
  private final ApiErrorFactory errorFactory;

  public ApiErrorResponseWriter(ObjectMapper objectMapper, ApiErrorFactory errorFactory) {
    this.objectMapper = objectMapper;
    this.errorFactory = errorFactory;
  }

  public void write(
      HttpServletRequest request,
      HttpServletResponse response,
      HttpStatus status,
      ApiErrorCode code,
      String message)
      throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(
        response.getOutputStream(), errorFactory.create(status, code, message, List.of(), request));
  }
}
