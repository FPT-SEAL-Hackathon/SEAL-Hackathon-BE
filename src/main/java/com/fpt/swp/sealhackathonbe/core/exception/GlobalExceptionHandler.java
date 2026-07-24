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

    @ExceptionHandler(RepositoryIntegrationException.class)
    public ResponseEntity<ErrorResponse> handleRepositoryIntegrationException(RepositoryIntegrationException ex) {
        HttpStatus status;
        switch (ex.getErrorCode()) {
            case INVALID_GITHUB_REPOSITORY_URL:
                status = HttpStatus.BAD_REQUEST;
                break;
            case INVALID_GITHUB_TOKEN:
                status = HttpStatus.UNAUTHORIZED;
                break;
            case GITHUB_REPOSITORY_FORBIDDEN:
            case EVENT_REPOSITORY_ACCESS_DENIED:
                status = HttpStatus.FORBIDDEN;
                break;
            case GITHUB_REPOSITORY_NOT_FOUND:
            case REPOSITORY_INTEGRATION_NOT_FOUND:
            case SUBMISSION_NOT_FOUND:
            case SUBMISSION_REPOSITORY_NOT_FOUND:
                status = HttpStatus.NOT_FOUND;
                break;
            case SUBMISSION_REPOSITORY_ACCESS_DENIED:
            case SUBMISSION_REPOSITORY_MODIFICATION_NOT_ALLOWED:
                status = HttpStatus.FORBIDDEN;
                break;
            case REPOSITORY_ALREADY_CONNECTED:
                status = HttpStatus.CONFLICT;
                break;
            case REPOSITORY_SYNC_ALREADY_RUNNING:
                status = HttpStatus.CONFLICT;
                break;
            case GITHUB_RATE_LIMITED:
                status = HttpStatus.TOO_MANY_REQUESTS;
                break;
            case GITHUB_UPSTREAM_ERROR:
                status = HttpStatus.BAD_GATEWAY;
                break;
            case GITHUB_TIMEOUT:
                status = HttpStatus.GATEWAY_TIMEOUT;
                break;
            case TOKEN_ENCRYPTION_CONFIGURATION_ERROR:
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
        }

        ResponseEntity.BodyBuilder builder = ResponseEntity.status(status);
        if (ex.getRetryAfter() != null) {
            builder.header("Retry-After", ex.getRetryAfter());
        }

        return builder.body(ErrorResponse.builder()
                .status(status.value())
                .error(ex.getErrorCode().name())
                .message(ex.getMessage() != null ? ex.getMessage() : "Repository integration error")
                .path(currentPath())
                .build());
    }

    /**
     * Loi tu GitHub metadata client (validate/preview, resync). Truoc day khong co handler
     * nen moi loi (repo private, 404, rate limit...) deu roi xuong handler generic thanh 500.
     * Message cua RepositoryMetadataException da la thong tin an toan (khong chua raw
     * response GitHub hay token) nen tra truc tiep cho FE.
     */
    @ExceptionHandler(com.fpt.swp.sealhackathonbe.integration.repository.exception.RepositoryMetadataException.class)
    public ResponseEntity<ErrorResponse> handleRepositoryMetadataException(
            com.fpt.swp.sealhackathonbe.integration.repository.exception.RepositoryMetadataException ex) {
        HttpStatus status;
        switch (ex.getErrorCode()) {
            case INVALID_GITHUB_REPOSITORY_URL:
                status = HttpStatus.BAD_REQUEST;
                break;
            case GITHUB_REPOSITORY_NOT_FOUND:
                status = HttpStatus.NOT_FOUND;
                break;
            case GITHUB_REPOSITORY_INACCESSIBLE:
            case PRIVATE_REPOSITORY_NOT_SUPPORTED:
                // Nghiep vu MVP chi ho tro repo public: private/inaccessible tra 422 de FE
                // phan biet voi loi phan quyen noi bo (403) cua chinh he thong.
                status = HttpStatus.UNPROCESSABLE_ENTITY;
                break;
            case GITHUB_RATE_LIMITED:
                status = HttpStatus.TOO_MANY_REQUESTS;
                break;
            case GITHUB_TIMEOUT:
                status = HttpStatus.GATEWAY_TIMEOUT;
                break;
            case GITHUB_UPSTREAM_ERROR:
            case GITHUB_INVALID_RESPONSE:
            default:
                status = HttpStatus.BAD_GATEWAY;
                break;
        }
        return build(
                status,
                ex.getErrorCode().name(),
                ex.getMessage() != null ? ex.getMessage() : "Repository metadata error",
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

    // Email đã thuộc user hiện có nhưng thiếu phương thức đăng nhập tương ứng:
    // KHÔNG tạo user thứ hai — trả linkingToken để client chạy luồng xác minh
    // (OTP email / mật khẩu) rồi liên kết vào đúng user đó.
    @ExceptionHandler(AccountLinkRequiredException.class)
    public ResponseEntity<ErrorResponse> handleAccountLinkRequired(AccountLinkRequiredException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("ACCOUNT_LINK_REQUIRED")
                .message(ex.getMessage() != null
                        ? ex.getMessage()
                        : "This email already belongs to an existing account. Verification is required to link sign-in methods")
                .path(currentPath())
                .details(Map.of(
                        "linkingToken", ex.getLinkingToken(),
                        "email", ex.getEmail()
                ))
                .build());
    }

    // Tài khoản đã bị xóa cứng (còn tombstone): báo user tạo tài khoản mới
    // thay vì trả "Invalid email or password" gây khó hiểu.
    @ExceptionHandler(AccountRemovedException.class)
    public ResponseEntity<ErrorResponse> handleAccountRemoved(AccountRemovedException ex) {
        return build(
                HttpStatus.GONE,
                "ACCOUNT_REMOVED",
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "This account has been removed. Please create a new account",
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

    // Hồ sơ trùng tài khoản khác: trả field trùng để frontend gợi ý liên kết,
    // tuyệt đối không auto-merge theo email.
    @ExceptionHandler(ProfileConflictException.class)
    public ResponseEntity<ErrorResponse> handleProfileConflict(ProfileConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("PROFILE_CONFLICT")
                .message(ex.getMessage() != null ? ex.getMessage() : "Profile conflicts with an existing account")
                .path(currentPath())
                .details(Map.of(
                        "conflictFields", ex.getConflictFields(),
                        "canLinkAccount", ex.isCanLinkAccount()
                ))
                .build());
    }

    // Tài khoản tạm đã có dữ liệu nghiệp vụ: chặn gộp tự động.
    @ExceptionHandler(MergeBlockedException.class)
    public ResponseEntity<ErrorResponse> handleMergeBlocked(MergeBlockedException ex) {
        return build(
                HttpStatus.CONFLICT,
                "MERGE_BLOCKED",
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "This account already has activity and requires manual support to merge",
                null
        );
    }

    // TEMPORARY/hồ sơ thiếu: chưa đủ điều kiện đăng ký sự kiện.
    @ExceptionHandler(ProfileIncompleteException.class)
    public ResponseEntity<ErrorResponse> handleProfileIncomplete(ProfileIncompleteException ex) {
        return build(
                HttpStatus.FORBIDDEN,
                "PROFILE_INCOMPLETE",
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "Please complete your profile before registering for an event",
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
        if (isDuplicateTeamNameViolation(ex)) {
            return build(HttpStatus.CONFLICT, "REGISTRATION_CONFLICT", "Team name already exists in this event", null);
        }

        return build(HttpStatus.BAD_REQUEST, "DATA_INTEGRITY_VIOLATION", "Request violates data constraints", null);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), null);
    }

    // Hai request cùng sửa/xóa một bản ghi (ví dụ 2 organizer hard-delete cùng
    // email): trả 409 để client retry, thay vì 500 do StaleObjectState.
    @ExceptionHandler(org.springframework.dao.OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            org.springframework.dao.OptimisticLockingFailureException ex) {
        return build(
                HttpStatus.CONFLICT,
                "CONCURRENT_MODIFICATION",
                "This record was changed by another request. Please refresh and try again.",
                null
        );
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

    private boolean isDuplicateTeamNameViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        if (message == null) {
            return false;
        }

        String normalized = message.toLowerCase();
        return normalized.contains("uq_teams_event_name")
                || (normalized.contains("teams")
                && normalized.contains("eventid")
                && normalized.contains("teamname"));
    }
}
