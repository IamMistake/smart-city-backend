package com.smartcity.springservice.service;

import java.util.List;
import java.util.UUID;

import com.smartcity.springservice.api.dto.IncidentRequest;
import com.smartcity.springservice.api.dto.IncidentResponse;
import com.smartcity.springservice.api.dto.IncidentUpdateRequest;
import com.smartcity.springservice.domain.core.enums.IncidentStatus;
import com.smartcity.springservice.domain.core.enums.PriorityLevel;

public interface IncidentService {
	IncidentResponse createIncident(IncidentRequest request);

	List<IncidentResponse> listIncidents(IncidentStatus status, PriorityLevel priority);

	IncidentResponse getIncident(UUID id);

	IncidentResponse updateIncident(UUID id, IncidentUpdateRequest request);
}
