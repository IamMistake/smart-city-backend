package com.smartcity.springservice.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.smartcity.springservice.domain.core.enums.IncidentType;
import com.smartcity.springservice.domain.core.enums.PriorityLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IncidentRequest(
	@NotBlank String title,
	String description,
	@NotNull PriorityLevel priority,
	@NotNull IncidentType type,
	@NotNull BigDecimal latitude,
	@NotNull BigDecimal longitude,
	String address,
	Instant occurredAt
) {}
