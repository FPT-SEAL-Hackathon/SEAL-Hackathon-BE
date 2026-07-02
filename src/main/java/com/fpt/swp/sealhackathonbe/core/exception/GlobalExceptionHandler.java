package com.fpt.swp.sealhackathonbe.core.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        String message = ex.getMessage() != null
                && !ex.getMessage().isBlank()
                && !"Access Denied".equals(ex.getMessage())
                ? ex.getMessage()
                : "You don't have permission to do this.";
        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED", message, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleParseError(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Invalid JSON format", null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        if ("eventId".equals(ex.getName())) {
            return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Invalid event id.", null);
        }
        if ("id".equals(ex.getName()) || "participantId".equals(ex.getName())) {
            return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Invalid participant id.", null);
        }
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Invalid request parameter.", null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (first, second) -> first
                ));

        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", errors);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        String path = currentPath();
        String message = path != null
                && (path.matches("/api/v1/events/[^/]+/participants/register")
                || path.matches("/api/v1/events/[^/]+/register"))
                ? "Authentication is required to register for an event."
                : "Invalid email or password";
        return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message, null);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return build(
                status,
                status.name(),
                ex.getReason() != null ? ex.getReason() : "Request failed",
                null
        );
    }

    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(Exception ex) {
        return build(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                ex.getMessage() != null ? ex.getMessage() : "Bad Request",
                null
        );
    }

    @ExceptionHandler(BusinessConflictException.class)
    public ResponseEntity<ErrorResponse> handleBusinessConflict(BusinessConflictException ex) {
        return build(
                HttpStatus.CONFLICT,
                "REGISTRATION_CONFLICT",
                ex.getMessage() != null ? ex.getMessage() : "Business conflict",
                null
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation at path {}", request.getRequestURI(), ex);
        if (isDuplicateEventNameViolation(ex)) {
            return build(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", "Event name already exists", null);
        }
        if (isDuplicateEventParticipantViolation(ex)) {
            return build(HttpStatus.CONFLICT, "REGISTRATION_CONFLICT", "You have already registered for this event.", null);
        }
        if (isDuplicateUserEmailViolation(ex)) {
            return build(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", "Email already exists.", null);
        }

        return build(HttpStatus.BAD_REQUEST, "DATA_INTEGRITY_VIOLATION", "Request violates data constraints", null);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at path {}", request.getRequestURI(), ex);
        String message = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage()
                : "An unknown error occurred";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_SERVER_ERROR")
                .message(message)
                .path(request.getRequestURI())
                .details(Map.of("exceptionClass", ex.getClass().getName()))
                .build());
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status,
            String error,
            String message,
            Map<String, String> errors
    ) {
        return ResponseEntity.status(status).body(ErrorResponse.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .path(currentPath())
                .details(Map.of())
                .errors(errors)
                .build());
    }

    private String currentPath() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest().getRequestURI();
        }
        return null;
    }

    private boolean isDuplicateEventNameViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        if (message == null) {
            return false;
        }

        String normalized = message.toLowerCase();
        return normalized.contains("uq_events_eventname_active")
                || (normalized.contains("events") && normalized.contains("eventname"))
                || normalized.contains("event name");
    }

    private boolean isDuplicateEventParticipantViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        if (message == null) {
            return false;
        }

        String normalized = message.toLowerCase();
        return normalized.contains("uq_eventparticipants_event_user")
                || (normalized.contains("eventparticipants")
                && normalized.contains("eventid")
                && normalized.contains("userid"));
    }

    private boolean isDuplicateUserEmailViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        if (message == null) {
            return false;
        }

        String normalized = message.toLowerCase();
        return normalized.contains("users")
                && (normalized.contains("email") || normalized.contains("uq_users_email"));
    }
}
