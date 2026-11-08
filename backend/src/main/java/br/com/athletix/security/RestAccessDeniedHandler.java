package br.com.athletix.security;

import br.com.athletix.shared.error.ApiErrorCode;
import br.com.athletix.shared.error.ApiErrorResponseWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ApiErrorResponseWriter errorWriter;

    public RestAccessDeniedHandler(ApiErrorResponseWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException, ServletException {
        errorWriter.write(request, response, HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN,
                "Você não tem permissão para acessar este recurso.");
    }
}
