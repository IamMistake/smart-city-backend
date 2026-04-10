package com.smartcity.springservice.api.dto;

import com.smartcity.springservice.domain.core.enums.IncidentType;
import com.smartcity.springservice.domain.core.enums.PriorityLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IncidentRequest(
	@NotBlank String title,
	String description,
	@NotNull PriorityLevel priority,
	@NotNull IncidentType type
) {}
