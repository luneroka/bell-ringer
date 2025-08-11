

package com.bell_ringer.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private Map<String, Object> body(HttpStatus status, String message, HttpServletRequest request) {
    Map<String, Object> map = new HashMap<>();
    map.put("timestamp", Instant.now().toString());
    map.put("status", status.value());
    map.put("error", status.getReasonPhrase());
    map.put("message", message);
    if (request != null) {
      map.put("path", request.getRequestURI());
      map.put("method", request.getMethod());
    }
    return map;
  }

  // 400 — invalid client input
  @ExceptionHandler({IllegalArgumentException.class})
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status).body(body(status, ex.getMessage(), req));
  }

  @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
  public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex, HttpServletRequest req) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status).body(body(status, ex.getMessage(), req));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String msg = ex.getBindingResult().getFieldErrors().stream()
        .map(err -> err.getField() + ": " + err.getDefaultMessage())
        .reduce((a, b) -> a + "; " + b)
        .orElse(ex.getMessage());
    return ResponseEntity.status(status).body(body(status, msg, req));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String msg = ex.getConstraintViolations().stream()
        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
        .reduce((a, b) -> a + "; " + b)
        .orElse(ex.getMessage());
    return ResponseEntity.status(status).body(body(status, msg, req));
  }

  // 404 — not found
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex, HttpServletRequest req) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    return ResponseEntity.status(status).body(body(status, ex.getMessage(), req));
  }

  // 409 — constraint / FK / unique violations
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
    HttpStatus status = HttpStatus.CONFLICT;
    String msg = (ex.getMostSpecificCause() != null) ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
    return ResponseEntity.status(status).body(body(status, msg, req));
  }

  // 500 — fallback
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, HttpServletRequest req) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(status).body(body(status, ex.getMessage(), req));
  }
}