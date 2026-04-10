package com.smartcity.springservice.api;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
			"error", "NOT_FOUND",
			"message", ex.getMessage() != null ? ex.getMessage() : "Resource not found",
			"status", 404
		));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, Object>> handleConflict(IllegalStateException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
			"error", "CONFLICT",
			"message", ex.getMessage(),
			"status", 409
		));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
			.map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
			.findFirst()
			.orElse("Validation failed");
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
			"error", "VALIDATION_ERROR",
			"message", message,
			"status", 422
		));
	}
}
