package com.smartcity.springservice.api;

import com.smartcity.springservice.dto.IncidentRequest;
import com.smartcity.springservice.dto.IncidentResponse;
import com.smartcity.springservice.dto.IncidentUpdateRequest;
import com.smartcity.springservice.model.IncidentPriority;
import com.smartcity.springservice.model.IncidentStatus;
import com.smartcity.springservice.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

	private final IncidentService incidentService;

	public IncidentController(IncidentService incidentService) {
		this.incidentService = incidentService;
	}

	@PostMapping
	public ResponseEntity<IncidentResponse> createIncident(@Valid @RequestBody IncidentRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.createIncident(request));
	}

	@GetMapping
	public ResponseEntity<List<IncidentResponse>> listIncidents(
			@RequestParam(required = false) IncidentStatus status,
			@RequestParam(required = false) IncidentPriority priority) {
		return ResponseEntity.ok(incidentService.listIncidents(status, priority));
	}

	@GetMapping("/{id}")
	public ResponseEntity<IncidentResponse> getIncident(@PathVariable Long id) {
		return ResponseEntity.ok(incidentService.getIncident(id));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<IncidentResponse> updateIncident(
			@PathVariable Long id,
			@RequestBody IncidentUpdateRequest request) {
		return ResponseEntity.ok(incidentService.updateIncident(id, request.getStatus(), request.getPriority()));
	}
}
