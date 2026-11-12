package br.com.athletix.shared.error;

import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ConflictException;
import br.com.athletix.shared.exception.ConcurrentModificationException;
import br.com.athletix.shared.exception.InvalidSortException;
import br.com.athletix.shared.exception.ResourceNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final ApiErrorFactory errorFactory;

    public ApiExceptionHandler(ApiErrorFactory errorFactory) {
        this.errorFactory = errorFactory;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<FieldErrorResponse> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        return response(HttpStatus.BAD_REQUEST, ApiErrorCode.INVALID_REQUEST,
                "A requisição contém dados inválidos.", fieldErrors, request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBinding(BindException exception, HttpServletRequest request) {
        List<FieldErrorResponse> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        return response(HttpStatus.BAD_REQUEST, ApiErrorCode.INVALID_REQUEST,
                "A requisição contém dados inválidos.", fieldErrors, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<FieldErrorResponse> fieldErrors = exception.getConstraintViolations().stream()
                .map(violation -> new FieldErrorResponse(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, ApiErrorCode.INVALID_REQUEST,
                "A requisição contém dados inválidos.", fieldErrors, request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ApiErrorResponse> handleUnreadableInput(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, ApiErrorCode.INVALID_REQUEST,
                "A requisição contém dados inválidos.", List.of(), request);
    }

    @ExceptionHandler(InvalidSortException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidSort(InvalidSortException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, ApiErrorCode.INVALID_SORT, exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRule(
            BusinessRuleException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.BAD_REQUEST, ApiErrorCode.BUSINESS_RULE_VIOLATION,
                exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND,
                exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUnmappedPath(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND,
                "Recurso não encontrado.", List.of(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, ApiErrorCode.RESOURCE_CONFLICT,
                exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler({OptimisticLockException.class, OptimisticLockingFailureException.class})
    public ResponseEntity<ApiErrorResponse> handleOptimisticLock(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, ApiErrorCode.CONCURRENT_MODIFICATION,
                "O recurso foi alterado por outra operação. Atualize os dados e tente novamente.", List.of(), request);
    }

    @ExceptionHandler(ConcurrentModificationException.class)
    public ResponseEntity<ApiErrorResponse> handleConcurrentModification(
            ConcurrentModificationException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.CONFLICT, ApiErrorCode.CONCURRENT_MODIFICATION,
                exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.CONFLICT, ApiErrorCode.RESOURCE_CONFLICT,
                "A operação conflita com dados existentes.", List.of(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {
        return response(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN,
                "Você não tem permissão para acessar este recurso.", List.of(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(AuthenticationException exception,
                                                                  HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, ApiErrorCode.UNAUTHENTICATED,
                "Credenciais inválidas.", List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.INTERNAL_ERROR,
                "Ocorreu um erro interno.", List.of(), request);
    }

    private FieldErrorResponse toFieldError(FieldError error) {
        return new FieldErrorResponse(error.getField(), error.getDefaultMessage());
    }

    private ResponseEntity<ApiErrorResponse> response(
            HttpStatus status,
            ApiErrorCode code,
            String message,
            List<FieldErrorResponse> fieldErrors,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status).body(errorFactory.create(status, code, message, fieldErrors, request));
    }
}
