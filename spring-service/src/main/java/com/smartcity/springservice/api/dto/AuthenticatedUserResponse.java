package com.smartcity.springservice.api.dto;

import com.smartcity.springservice.domain.core.enums.UserRole;
import java.util.UUID;

public record AuthenticatedUserResponse(
		UUID id,
		String clerkUserId,
		String email,
		String fullName,
		UserRole role,        // ← String → UserRole
		String avatarUrl,
		boolean isActive
) {}