package com.smartcity.springservice.api;

import com.smartcity.springservice.api.dto.AuthenticatedUserResponse;
import com.smartcity.springservice.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final CurrentUserService currentUserService;

	public AuthController(CurrentUserService currentUserService) {
		this.currentUserService = currentUserService;
	}

	@GetMapping("/me")
	public AuthenticatedUserResponse getAuthenticatedUser(@AuthenticationPrincipal Jwt jwt) {
		if (jwt == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authentication token");
		}

		String clerkUserId = jwt.getSubject();
		String email = getRequiredClaim(jwt, "email");
		String fullName = getOptionalClaim(jwt, "name");
		String avatarUrl = getOptionalClaim(jwt, "picture");

		if (clerkUserId == null || clerkUserId.isBlank()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token subject is missing");
		}

		currentUserService.provisionUserIfMissing(clerkUserId, email, fullName, avatarUrl);

		return currentUserService.resolveActiveUser(clerkUserId);
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
