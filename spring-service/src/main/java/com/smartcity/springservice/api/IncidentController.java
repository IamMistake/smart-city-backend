package com.smartcity.springservice.api;

import com.smartcity.springservice.api.dto.IncidentResponse;
import com.smartcity.springservice.api.dto.IncidentRequest;
import com.smartcity.springservice.api.dto.IncidentUpdateRequest;
import com.smartcity.springservice.domain.core.enums.IncidentStatus;
import com.smartcity.springservice.domain.core.enums.PriorityLevel;
import com.smartcity.springservice.service.IncidentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;




@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
	private final IncidentService incidentService;

	public IncidentController(IncidentService incidentService) {
		this.incidentService = incidentService;
	}

	@PostMapping
	@PreAuthorize("@roleGuard.userHasAnyRole('CITIZEN', 'OPERATOR', 'AUTHORITY', 'ADMIN')")
	public ResponseEntity<IncidentResponse> createIncident(@Valid @RequestBody IncidentRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.createIncident(request));
	}

	@GetMapping
	@PreAuthorize("@roleGuard.userHasAnyRole('CITIZEN', 'OPERATOR', 'AUTHORITY', 'ADMIN')")
	public ResponseEntity<List<IncidentResponse>> listIncidents(
		@RequestParam(required = false) IncidentStatus status,
		@RequestParam(required = false) PriorityLevel priority
	) {
		return ResponseEntity.ok(incidentService.listIncidents(status, priority));
	}

	@GetMapping("/{id}")
	@PreAuthorize("@roleGuard.userHasAnyRole('CITIZEN', 'OPERATOR', 'AUTHORITY', 'ADMIN')")
	public ResponseEntity<IncidentResponse> getIncident(@PathVariable UUID id) {
		return ResponseEntity.ok(incidentService.getIncident(id));
	}

	@PatchMapping("/{id}")
	@PreAuthorize("@roleGuard.userHasAnyRole('CITIZEN', 'OPERATOR', 'AUTHORITY', 'ADMIN')")
	public ResponseEntity<IncidentResponse> updateIncident(
		@PathVariable UUID id,
		@RequestBody IncidentUpdateRequest request
	) {
		return ResponseEntity.ok(incidentService.updateIncident(id, request));
	}
}
