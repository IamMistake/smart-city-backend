package com.smartcity.springservice.service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcity.springservice.api.dto.IncidentRequest;
import com.smartcity.springservice.api.dto.IncidentResponse;
import com.smartcity.springservice.api.dto.IncidentUpdateRequest;
import com.smartcity.springservice.domain.core.entity.Incident;
import com.smartcity.springservice.domain.core.entity.IncidentStatusHistory;
import com.smartcity.springservice.domain.core.enums.IncidentStatus;
import com.smartcity.springservice.domain.core.enums.PriorityLevel;
import com.smartcity.springservice.domain.core.repository.IncidentRepository;
import com.smartcity.springservice.domain.core.repository.IncidentStatusHistoryRepository;

@Service
public class IncidentServiceImpl implements IncidentService {
	private final IncidentRepository incidentRepository;
	private final IncidentStatusHistoryRepository statusHistoryRepository;

	public IncidentServiceImpl(
		IncidentRepository incidentRepository,
		IncidentStatusHistoryRepository statusHistoryRepository
	) {
		this.incidentRepository = incidentRepository;
		this.statusHistoryRepository = statusHistoryRepository;
	}

	@Override
	public IncidentResponse createIncident(IncidentRequest request) {
		Incident incident = new Incident();
		incident.setTitle(request.title());
		incident.setDescription(request.description());
		incident.setPriority(request.priority());
		incident.setIncidentType(request.type());
		incident.setStatus(IncidentStatus.ACTIVE);
		return toResponse(incidentRepository.save(incident));
	}

	@Override
	public List<IncidentResponse> listIncidents(IncidentStatus status, PriorityLevel priority) {
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
	public IncidentResponse getIncident(UUID id) {
		Incident incident = incidentRepository.findById(id)
			.orElseThrow(NoSuchElementException::new);
		return toResponse(incident);
	}

	@Override
	@Transactional
	public IncidentResponse updateIncident(UUID id, IncidentUpdateRequest request) {
		Incident incident = incidentRepository.findById(id)
			.orElseThrow(NoSuchElementException::new);

		IncidentStatus currentStatus = incident.getStatus();

		if (request.status() != null) {
			if (currentStatus == IncidentStatus.RESOLVED && request.status() == IncidentStatus.ACTIVE) {
				throw new IllegalStateException("A resolved incident cannot be re-opened.");
			}
			if (request.status() == IncidentStatus.RESOLVED && currentStatus != IncidentStatus.RESOLVED) {
				incident.setResolvedAt(Instant.now());
				IncidentStatusHistory history = new IncidentStatusHistory();
				history.setIncident(incident);
				history.setOldStatus(currentStatus);
				history.setNewStatus(IncidentStatus.RESOLVED);
				statusHistoryRepository.save(history);
			}
			incident.setStatus(request.status());
		}

		if (request.priority() != null) {
			incident.setPriority(request.priority());
		}

		return toResponse(incidentRepository.save(incident));
	}

	private IncidentResponse toResponse(Incident incident) {
		return new IncidentResponse(
			incident.getId(),
			incident.getTitle(),
			incident.getDescription(),
			incident.getPriority(),
			incident.getIncidentType(),
			incident.getStatus(),
			incident.getCreatedAt(),
			incident.getUpdatedAt(),
			incident.getResolvedAt()
		);
	}
}
