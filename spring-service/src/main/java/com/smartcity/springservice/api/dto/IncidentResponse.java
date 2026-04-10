package com.smartcity.springservice.api.dto;

import java.time.Instant;
import java.util.UUID;

import com.smartcity.springservice.domain.core.enums.IncidentStatus;
import com.smartcity.springservice.domain.core.enums.IncidentType;
import com.smartcity.springservice.domain.core.enums.PriorityLevel;

public record IncidentResponse(
	UUID id,
	String title,
	String description,
	PriorityLevel priority,
	IncidentType type,
	IncidentStatus status,
	Instant createdAt,
	Instant updatedAt,
	Instant resolvedAt
) {}
