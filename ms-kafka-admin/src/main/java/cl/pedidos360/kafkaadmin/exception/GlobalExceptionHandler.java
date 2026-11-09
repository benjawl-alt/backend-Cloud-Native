package cl.pedidos360.kafkaadmin.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail onBody(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos");
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail onParam(ConstraintViolationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> errors.put(v.getPropertyPath().toString(), v.getMessage()));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Parámetros inválidos");
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail onMethodValidation(HandlerMethodValidationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getParameterValidationResults().forEach(r -> r.getResolvableErrors()
                .forEach(e -> errors.put(r.getMethodParameter().getParameterName(), e.getDefaultMessage())));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Parámetros inválidos");
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail onIllegal(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail onNotFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail onConflict(ConflictException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(KafkaUnavailableException.class)
    public ProblemDetail onKafkaDown(KafkaUnavailableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }
}