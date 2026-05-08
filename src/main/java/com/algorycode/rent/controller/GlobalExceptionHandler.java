package com.algorycode.rent.controller;

import com.algorycode.rent.exception.BadRequestException;
import com.algorycode.rent.exception.ConflictException;
import com.algorycode.rent.exception.ResourceNotFoundException;
import com.algorycode.rent.logging.AuditLog;
import com.algorycode.rent.logging.SafeReasonCodes;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * HTTP hata eşlemesi yalnızca {@code api.web} katmanında; iş kuralları istisnaları ProblemDetail
 * olarak döner. KVKK: ham exception metni audit loga yazılmaz.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private final AuditLog auditLog;

  /**
   * Bean Validation (@Valid) — alan hataları FE’de {@code fieldErrors} ile gösterilir; tekrarlayan
   * null guard’a gerek yok.
   */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      @NonNull MethodArgumentNotValidException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {
    auditLog.warnBusiness(
        SafeReasonCodes.VALIDATION_FAILED,
        Map.of(
            "httpStatus", String.valueOf(HttpStatus.BAD_REQUEST.value()),
            "exceptionType", ex.getClass().getSimpleName(),
            "fieldErrorCount", String.valueOf(ex.getBindingResult().getFieldErrorCount())));
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "İstek doğrulanamadı.");
    pd.setTitle("Validation Failed");
    Map<String, String> fieldErrors = new LinkedHashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      String msg = fe.getDefaultMessage();
      fieldErrors.put(fe.getField(), msg != null && !msg.isBlank() ? msg : "Geçersiz değer");
    }
    pd.setProperty("fieldErrors", fieldErrors);
    return ResponseEntity.badRequest().body(pd);
  }

  /** Metot / sınıf düzeyi {@code jakarta.validation} ihlalleri. */
  @ExceptionHandler(ConstraintViolationException.class)
  ProblemDetail constraintViolation(ConstraintViolationException ex) {
    auditLog.warnBusiness(
        SafeReasonCodes.VALIDATION_FAILED,
        Map.of(
            "httpStatus", String.valueOf(HttpStatus.BAD_REQUEST.value()),
            "exceptionType", ex.getClass().getSimpleName(),
            "fieldErrorCount", String.valueOf(ex.getConstraintViolations().size())));
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "İstek doğrulanamadı.");
    pd.setTitle("Validation Failed");
    Map<String, String> fieldErrors = new LinkedHashMap<>();
    for (ConstraintViolation<?> cv : ex.getConstraintViolations()) {
      fieldErrors.put(cv.getPropertyPath().toString(), cv.getMessage());
    }
    pd.setProperty("fieldErrors", fieldErrors);
    return pd;
  }

  /**
   * Beklenmeyen program tutarsızlığı (ör. doğrulama atlanmış servis çağrısı); ayrıntı istemciye
   * sızdırılmaz.
   */
  @ExceptionHandler(IllegalStateException.class)
  ProblemDetail illegalState(IllegalStateException ex) {
    auditLog.errorTechnical(
        SafeReasonCodes.INVARIANT_VIOLATION,
        ex,
        Map.of("httpStatus", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())));
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Sunucu tutarlılık hatası. Lütfen destek ile iletişime geçin.");
    pd.setTitle("Internal Server Error");
    return pd;
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  ProblemDetail notFound(ResourceNotFoundException ex) {
    auditLog.warnBusiness(
        SafeReasonCodes.RESOURCE_NOT_FOUND,
        Map.of(
            "httpStatus", String.valueOf(HttpStatus.NOT_FOUND.value()),
            "exceptionType", ex.getClass().getSimpleName()));
    var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    pd.setTitle("Not Found");
    return pd;
  }

  @ExceptionHandler(ConflictException.class)
  ProblemDetail conflict(ConflictException ex) {
    auditLog.warnBusiness(
        SafeReasonCodes.CONFLICT,
        Map.of(
            "httpStatus", String.valueOf(HttpStatus.CONFLICT.value()),
            "exceptionType", ex.getClass().getSimpleName()));
    var pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    pd.setTitle("Conflict");
    return pd;
  }

  @ExceptionHandler(BadRequestException.class)
  ProblemDetail badRequest(BadRequestException ex) {
    auditLog.warnBusiness(
        SafeReasonCodes.BAD_REQUEST,
        Map.of(
            "httpStatus", String.valueOf(HttpStatus.BAD_REQUEST.value()),
            "exceptionType", ex.getClass().getSimpleName()));
    var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    pd.setTitle("Bad Request");
    return pd;
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail fallback(Exception ex) {
    auditLog.errorTechnical(SafeReasonCodes.UNHANDLED_EXCEPTION, ex, Map.of());
    var pd =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "Beklenmeyen bir hata oluştu.");
    pd.setTitle("Internal Server Error");
    return pd;
  }
}
