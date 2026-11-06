package kz.narxoz.vrs.handler;

import jakarta.servlet.http.HttpServletRequest;
import kz.narxoz.vrs.domain.DuplicateVehicleRequest;
import kz.narxoz.vrs.domain.VehicleRequestNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.UUID;

// одно место, где исключение превращается в статус и ApiError
@RestControllerAdvice
public class ApiErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiErrorHandler.class);

    // 400: пустое поле или слишком длинное, например "purpose must not be blank"
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalidBody(MethodArgumentNotValidException e, HttpServletRequest request) {
        var field = e.getBindingResult().getFieldErrors().get(0);
        return error(HttpStatus.BAD_REQUEST, "VALIDATION", field.getField() + " " + field.getDefaultMessage(), request);
    }

    // 400: в пути не UUID (например номер VR-1042 вместо id)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> badId(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION", "id must be a UUID", request);
    }

    // 400: тело не JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> notJson(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION", "body must be JSON", request);
    }

    // 404: UUID нормальный, но такой заявки нет
    @ExceptionHandler(VehicleRequestNotFound.class)
    ResponseEntity<ApiError> notFound(HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "vehicle request not found", request);
    }

    // 404: такого адреса нет (например закрытый /actuator/env)
    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiError> noPath(HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "path not found", request);
    }

    // 409: номер заявки уже есть, первая заявка остаётся
    @ExceptionHandler(DuplicateVehicleRequest.class)
    ResponseEntity<ApiError> duplicate(HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "DUPLICATE", "vehicle request with this business key already exists", request);
    }

    // 405: например PUT, которого у нас нет
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiError> wrongMethod(HttpServletRequest request) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "method not allowed", request);
    }

    // 415: Content-Type не application/json
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiError> wrongType(HttpServletRequest request) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED", "content type must be application/json", request);
    }

    // 500: всё остальное. Текст исключения только в лог, клиенту нет
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception e, HttpServletRequest request) {
        ResponseEntity<ApiError> response = error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL", "unexpected error", request);
        log.error("unexpected error, correlationId={}", response.getBody().correlationId(), e);
        return response;
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message, HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-Id");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        return ResponseEntity.status(status).body(new ApiError(code, message, correlationId));
    }
}
