package com.smartcity.springservice.api;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

	@GetMapping("/")
	public ResponseEntity<Map<String, Object>> health() {
		return ResponseEntity.ok(
				Map.of(
						"status", "UP",
						"service", "spring-service",
						"timestamp", Instant.now().toString()
				)
		);
	}
}
