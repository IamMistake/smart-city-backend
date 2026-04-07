package com.smartcity.springservice.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
			fieldErrors.put(fe.getField(), fe.getDefaultMessage());
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("error", "Validation failed");
		body.put("message", fieldErrors);
		body.put("status", 422);
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("error", "Not found");
		body.put("message", ex.getMessage());
		body.put("status", 404);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}
}
