package com.smartcity.springservice.service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.smartcity.springservice.api.dto.IncidentRequest;
import com.smartcity.springservice.api.dto.IncidentResponse;
import com.smartcity.springservice.api.dto.IncidentUpdateRequest;
import com.smartcity.springservice.domain.core.entity.Incident;
import com.smartcity.springservice.domain.core.entity.IncidentStatusHistory;
import com.smartcity.springservice.domain.core.entity.UserProfile;
import com.smartcity.springservice.domain.core.enums.IncidentStatus;
import com.smartcity.springservice.domain.core.enums.PriorityLevel;
import com.smartcity.springservice.domain.core.repository.IncidentRepository;
import com.smartcity.springservice.domain.core.repository.IncidentStatusHistoryRepository;
import com.smartcity.springservice.domain.core.repository.UserProfileRepository;

@Service
public class IncidentServiceImpl implements IncidentService {
	private final IncidentRepository incidentRepository;
	private final IncidentStatusHistoryRepository statusHistoryRepository;
	private final UserProfileRepository userProfileRepository;
	private final CurrentUserService currentUserService;

	public IncidentServiceImpl(
		IncidentRepository incidentRepository,
		IncidentStatusHistoryRepository statusHistoryRepository,
		UserProfileRepository userProfileRepository,
		CurrentUserService currentUserService
	) {
		this.incidentRepository = incidentRepository;
		this.statusHistoryRepository = statusHistoryRepository;
		this.userProfileRepository = userProfileRepository;
		this.currentUserService = currentUserService;
	}

	@Override
	public IncidentResponse createIncident(IncidentRequest request) {
		Jwt jwt = currentJwt();
		String clerkUserId = jwt.getSubject();
		String email = getRequiredClaim(jwt, "email");

		currentUserService.provisionUserIfMissing(
			clerkUserId,
			email,
			getOptionalClaim(jwt, "name"),
			getOptionalClaim(jwt, "picture")
		);

		UserProfile reportedByUser = userProfileRepository
			.findByClerkUserIdAndDeletedAtIsNull(clerkUserId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User profile not found"));

		Incident incident = new Incident();
		incident.setReportedByUser(reportedByUser);
		incident.setTitle(request.title());
		incident.setDescription(request.description());
		incident.setPriority(request.priority());
		incident.setIncidentType(request.type());
		incident.setLatitude(request.latitude());
		incident.setLongitude(request.longitude());
		incident.setAddress(request.address());
		incident.setOccurredAt(request.occurredAt());
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

	private Jwt currentJwt() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authentication token");
		}

		if (jwt.getSubject() == null || jwt.getSubject().isBlank()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token subject is missing");
		}

		return jwt;
	}

	private String getRequiredClaim(Jwt jwt, String claimName) {
		String value = jwt.getClaimAsString(claimName);
		if (value == null || value.isBlank()) {
			throw new ResponseStatusException(
				HttpStatus.UNAUTHORIZED,
				"Token claim '%s' is missing".formatted(claimName)
			);
		}

		return value;
	}

	private String getOptionalClaim(Jwt jwt, String claimName) {
		String value = jwt.getClaimAsString(claimName);
		if (value == null || value.isBlank()) {
			return null;
		}

		return value;
	}
}
