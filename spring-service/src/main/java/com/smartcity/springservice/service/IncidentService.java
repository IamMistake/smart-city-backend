package com.smartcity.springservice.service;

import com.smartcity.springservice.dto.IncidentRequest;
import com.smartcity.springservice.dto.IncidentResponse;
import com.smartcity.springservice.model.IncidentPriority;
import com.smartcity.springservice.model.IncidentStatus;
import java.util.List;

public interface IncidentService {
	IncidentResponse createIncident(IncidentRequest request);
	List<IncidentResponse> listIncidents(IncidentStatus status, IncidentPriority priority);
	IncidentResponse getIncident(Long id);
	IncidentResponse updateIncident(Long id, IncidentStatus status, IncidentPriority priority);
}
