package com.algorycode.rent.api.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ConflictException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.logging.AuditLog;
import com.algorycode.rent.logging.SafeReasonCodes;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  @Mock private AuditLog auditLog;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new ExceptionProbeController())
            .setControllerAdvice(new GlobalExceptionHandler(auditLog))
            .build();
  }

  @Test
  void resourceNotFound_mapsTo404ProblemDetail() throws Exception {
    mockMvc
        .perform(get("/__probe/not-found").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("missing"))
        .andExpect(jsonPath("$.status").value(404));

    verify(auditLog)
        .warnBusiness(
            eq(SafeReasonCodes.RESOURCE_NOT_FOUND),
            eq(
                Map.of(
                    "httpStatus", "404",
                    "exceptionType", "ResourceNotFoundException")));
  }

  @Test
  void conflict_mapsTo409ProblemDetail() throws Exception {
    mockMvc
        .perform(get("/__probe/conflict").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.title").value("Conflict"))
        .andExpect(jsonPath("$.detail").value("dup"))
        .andExpect(jsonPath("$.status").value(409));

    verify(auditLog)
        .warnBusiness(
            eq(SafeReasonCodes.CONFLICT),
            eq(
                Map.of(
                    "httpStatus", "409",
                    "exceptionType", "ConflictException")));
  }

  @Test
  void badRequest_mapsTo400ProblemDetail() throws Exception {
    mockMvc
        .perform(get("/__probe/bad").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.detail").value("invalid"))
        .andExpect(jsonPath("$.status").value(400));

    verify(auditLog)
        .warnBusiness(
            eq(SafeReasonCodes.BAD_REQUEST),
            eq(
                Map.of(
                    "httpStatus", "400",
                    "exceptionType", "BadRequestException")));
  }

  @Test
  void unhandledException_mapsTo500AndLogsTechnical() throws Exception {
    mockMvc
        .perform(get("/__probe/boom").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.title").value("Internal Server Error"))
        .andExpect(jsonPath("$.detail").value("Beklenmeyen bir hata oluştu."))
        .andExpect(jsonPath("$.status").value(500));

    verify(auditLog)
        .errorTechnical(
            eq(SafeReasonCodes.UNHANDLED_EXCEPTION), any(RuntimeException.class), eq(Map.of()));
  }

  @RestController
  @RequestMapping("/__probe")
  static class ExceptionProbeController {

    @GetMapping("/not-found")
    void notFound() {
      throw new ResourceNotFoundException("missing");
    }

    @GetMapping("/conflict")
    void conflict() {
      throw new ConflictException("dup");
    }

    @GetMapping("/bad")
    void bad() {
      throw new BadRequestException("invalid");
    }

    @GetMapping("/boom")
    void boom() {
      throw new RuntimeException("secret PII must not reach audit log");
    }
  }
}
