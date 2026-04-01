package com.smartcity.springservice.api;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.smartcity.springservice.api.dto.AuthenticatedUserResponse;
import com.smartcity.springservice.service.CurrentUserService;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CurrentUserService currentUserService;

	@Test
	void healthEndpointShouldBePublic() throws Exception {
		mockMvc.perform(get("/api/health/")).andExpect(status().isOk());
	}

	@Test
	void authMeShouldRequireAuthentication() throws Exception {
		mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
	}

	@Test
	void authMeShouldReturnActiveUser() throws Exception {
		AuthenticatedUserResponse user = new AuthenticatedUserResponse(
			UUID.fromString("95e9e14c-8e98-4ad3-818f-2f1bcc7dd4ab"),
			"user_test_123",
			"user@example.com",
			"Test User",
			"CITIZEN",
			"https://example.com/avatar.png",
			true
		);

		when(currentUserService.resolveActiveUser("user_test_123")).thenReturn(user);

		mockMvc
			.perform(
				get("/api/auth/me")
					.with(
						jwt().jwt((jwt) -> jwt.subject("user_test_123").claim("email", "user@example.com"))
					)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.clerkUserId").value("user_test_123"))
			.andExpect(jsonPath("$.email").value("user@example.com"))
			.andExpect(jsonPath("$.isActive").value(true));
	}

	@Test
	void authMeShouldRejectInactiveUser() throws Exception {
		when(currentUserService.resolveActiveUser("user_test_123")).thenThrow(
			new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not active")
		);

		mockMvc
			.perform(
				get("/api/auth/me")
					.with(
						jwt().jwt((jwt) -> jwt.subject("user_test_123").claim("email", "user@example.com"))
					)
			)
			.andExpect(status().isForbidden())
			.andExpect(status().reason("User is not active"));
	}
}
