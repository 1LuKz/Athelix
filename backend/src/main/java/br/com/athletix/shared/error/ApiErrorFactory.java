package br.com.athletix.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ApiErrorFactory {

  public ApiErrorResponse create(
      HttpStatus status,
      ApiErrorCode code,
      String message,
      List<FieldErrorResponse> fieldErrors,
      HttpServletRequest request) {
    return new ApiErrorResponse(
        status.value(), code.name(), message, fieldErrors, request.getRequestURI(), Instant.now());
  }
}
