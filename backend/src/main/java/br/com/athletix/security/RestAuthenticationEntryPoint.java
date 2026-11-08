package br.com.athletix.security;

import br.com.athletix.shared.error.ApiErrorCode;
import br.com.athletix.shared.error.ApiErrorResponseWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ApiErrorResponseWriter errorWriter;

    public RestAuthenticationEntryPoint(ApiErrorResponseWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        errorWriter.write(request, response, HttpStatus.UNAUTHORIZED, ApiErrorCode.UNAUTHENTICATED,
                "Autenticação necessária para acessar este recurso.");
    }
}
