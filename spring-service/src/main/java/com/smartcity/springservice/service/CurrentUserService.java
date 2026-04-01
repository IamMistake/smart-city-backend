package com.smartcity.springservice.service;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.smartcity.springservice.api.dto.AuthenticatedUserResponse;
import com.smartcity.springservice.domain.core.enums.UserRole;

@Service
public class CurrentUserService {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	public CurrentUserService(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public AuthenticatedUserResponse resolveActiveUser(String clerkUserId) {
		AuthenticatedUserResponse user = jdbcTemplate.query(
			"""
			SELECT
				id,
				clerk_user_id,
				email,
				full_name,
				role,
				avatar_url,
				is_active
			FROM core.user_profiles
			WHERE clerk_user_id = :clerkUserId
				AND deleted_at IS NULL
			LIMIT 1
			""",
			Map.of("clerkUserId", clerkUserId),
			(rs, rowNum) -> new AuthenticatedUserResponse(
				(UUID) rs.getObject("id"),
				rs.getString("clerk_user_id"),
				rs.getString("email"),
				rs.getString("full_name"),
				rs.getString("role"),
				rs.getString("avatar_url"),
				rs.getBoolean("is_active")
			)
		).stream().findFirst().orElseThrow(() ->
			new ResponseStatusException(HttpStatus.FORBIDDEN, "User profile not found")
		);

		if (!user.isActive()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not active");
		}

		return user;
	}

	public void provisionUserIfMissing(
		String clerkUserId,
		String email,
		String fullName,
		String avatarUrl
	) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("clerkUserId", clerkUserId);
		parameters.put("email", email);
		parameters.put("fullName", fullName);
		parameters.put("role", UserRole.CITIZEN.name());
		parameters.put("avatarUrl", avatarUrl);

		jdbcTemplate.update(
			"""
			INSERT INTO core.user_profiles (
				clerk_user_id,
				email,
				full_name,
				role,
				avatar_url,
				is_active
			)
			VALUES (
				:clerkUserId,
				:email,
				:fullName,
				CAST(:role AS core.user_role),
				:avatarUrl,
				TRUE
			)
			ON CONFLICT (clerk_user_id) DO UPDATE
			SET
				email = EXCLUDED.email,
				full_name = EXCLUDED.full_name,
				avatar_url = EXCLUDED.avatar_url,
				deleted_at = NULL
			""",
			parameters
		);
	}
}
