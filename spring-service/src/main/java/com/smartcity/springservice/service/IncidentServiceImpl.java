package com.smartcity.springservice.service;

import com.smartcity.springservice.dto.IncidentRequest;
import com.smartcity.springservice.dto.IncidentResponse;
import com.smartcity.springservice.model.Incident;
import com.smartcity.springservice.model.IncidentPriority;
import com.smartcity.springservice.model.IncidentStatus;
import com.smartcity.springservice.repository.IncidentRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class IncidentServiceImpl implements IncidentService {

	private final IncidentRepository incidentRepository;

	public IncidentServiceImpl(IncidentRepository incidentRepository) {
		this.incidentRepository = incidentRepository;
	}

	@Override
	public IncidentResponse createIncident(IncidentRequest request) {
		Incident incident = new Incident();
		incident.setTitle(request.getTitle());
		incident.setDescription(request.getDescription());
		incident.setPriority(request.getPriority());
		return toResponse(incidentRepository.save(incident));
	}

	@Override
	public List<IncidentResponse> listIncidents(IncidentStatus status, IncidentPriority priority) {
		List<Incident> incidents;
		if (status != null && priority != null) {
			incidents = incidentRepository.findByStatusAndPriority(status, priority);
		} else if (status != null) {
			incidents = incidentRepository.findByStatus(status);
		} else if (priority != null) {
			incidents = incidentRepository.findByPriority(priority);
		} else {
			incidents = incidentRepository.findAll();
		}
		return incidents.stream().map(this::toResponse).toList();
	}

	@Override
	public IncidentResponse getIncident(Long id) {
		return incidentRepository.findById(id)
			.map(this::toResponse)
			.orElseThrow(() -> new NoSuchElementException("Incident not found with id: " + id));
	}

	@Override
	public IncidentResponse updateIncident(Long id, IncidentStatus status, IncidentPriority priority) {
		Incident incident = incidentRepository.findById(id)
			.orElseThrow(() -> new NoSuchElementException("Incident not found with id: " + id));

		if (status != null) {
			incident.setStatus(status);
			if (status == IncidentStatus.RESOLVED && incident.getResolvedAt() == null) {
				incident.setResolvedAt(LocalDateTime.now());
			}
		}
		if (priority != null) {
			incident.setPriority(priority);
		}

		return toResponse(incidentRepository.save(incident));
	}

	private IncidentResponse toResponse(Incident incident) {
		IncidentResponse response = new IncidentResponse();
		response.setId(incident.getId());
		response.setTitle(incident.getTitle());
		response.setDescription(incident.getDescription());
		response.setPriority(incident.getPriority());
		response.setStatus(incident.getStatus());
		response.setCreatedAt(incident.getCreatedAt());
		response.setUpdatedAt(incident.getUpdatedAt());
		response.setResolvedAt(incident.getResolvedAt());
		return response;
	}
}
