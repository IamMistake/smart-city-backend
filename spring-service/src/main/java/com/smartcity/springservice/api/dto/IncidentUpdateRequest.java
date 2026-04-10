package com.smartcity.springservice.api.dto;

import com.smartcity.springservice.domain.core.enums.IncidentStatus;
import com.smartcity.springservice.domain.core.enums.PriorityLevel;

public record IncidentUpdateRequest(
	IncidentStatus status,
	PriorityLevel priority
) {}
