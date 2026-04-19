package com.algorycode.rent.api.web;

import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ConflictException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.logging.AuditLog;
import com.algorycode.rent.logging.SafeReasonCodes;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * HTTP hata eşlemesi yalnızca {@code api.web} katmanında; iş kuralları istisnaları ProblemDetail olarak döner.
 * KVKK: ham exception metni audit loga yazılmaz.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private final AuditLog auditLog;

  public GlobalExceptionHandler(AuditLog auditLog) {
    this.auditLog = auditLog;
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
