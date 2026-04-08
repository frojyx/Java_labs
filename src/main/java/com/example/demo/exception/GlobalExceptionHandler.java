package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BULK_DEMO_FAILURE_MESSAGE = "Artificial failure after saving the first order in bulk.";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception,
                                                           HttpServletRequest request) {
        logApiException(HttpStatus.NOT_FOUND, exception.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException exception,
                                                             HttpServletRequest request) {
        logApiException(HttpStatus.BAD_REQUEST, exception.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request, null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException exception,
                                                           HttpServletRequest request) {
        logApiException(HttpStatus.CONFLICT, exception.getMessage());
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request, null);
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ApiErrorResponse> handleUnprocessable(UnprocessableEntityException exception,
                                                                HttpServletRequest request) {
        logApiException(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception,
                                                             HttpServletRequest request) {
        List<ApiValidationError> validationErrors = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::mapFieldError)
            .toList();

        logApiException(HttpStatus.BAD_REQUEST, "Validation error in request body");

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "Validation error in request body",
            request,
            validationErrors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception,
                                                                      HttpServletRequest request) {
        List<ApiValidationError> validationErrors = exception.getConstraintViolations()
            .stream()
            .map(violation -> new ApiValidationError(violation.getPropertyPath().toString(), violation.getMessage()))
            .toList();

        logApiException(HttpStatus.BAD_REQUEST, "Validation error in request parameters");

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "Validation error in request parameters",
            request,
            validationErrors
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception,
                                                               HttpServletRequest request) {
        String message = "Invalid value for parameter '" + exception.getName() + "'";
        logApiException(HttpStatus.BAD_REQUEST, message);
        return buildResponse(HttpStatus.BAD_REQUEST, message, request, null);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandlerFound(NoHandlerFoundException exception,
                                                                 HttpServletRequest request) {
        logApiException(HttpStatus.NOT_FOUND, "Endpoint not found");
        return buildResponse(HttpStatus.NOT_FOUND, "Endpoint not found", request, null);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntime(RuntimeException exception,
                                                          HttpServletRequest request) {
        if (BULK_DEMO_FAILURE_MESSAGE.equals(exception.getMessage())) {
            logApiException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        } else {
            logApiException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception);
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception exception,
                                                                   HttpServletRequest request) {
        logApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", exception);
        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error",
            request,
            null
        );
    }

    private ApiValidationError mapFieldError(FieldError fieldError) {
        return new ApiValidationError(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private void logApiException(HttpStatus status, String message) {
        LOGGER.warn("API exception [{}]: {}", status.value(), message);
    }

    private void logApiException(HttpStatus status, String message, Exception exception) {
        LOGGER.error("API exception [{}]: {}", status.value(), message, exception);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message,
                                                           HttpServletRequest request,
                                                           List<ApiValidationError> validationErrors) {
        ApiErrorResponse body = new ApiErrorResponse(
            LocalDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            request.getRequestURI(),
            validationErrors
        );
        return ResponseEntity.status(status).body(body);
    }
}
