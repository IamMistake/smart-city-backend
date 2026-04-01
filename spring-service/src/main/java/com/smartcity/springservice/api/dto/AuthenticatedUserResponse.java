package com.smartcity.springservice.api.dto;

import java.util.UUID;

public record AuthenticatedUserResponse(
	UUID id,
	String clerkUserId,
	String email,
	String fullName,
	String role,
	String avatarUrl,
	boolean isActive
) {}
