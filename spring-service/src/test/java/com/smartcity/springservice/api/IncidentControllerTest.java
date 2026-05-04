package com.smartcity.springservice.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.smartcity.springservice.api.dto.AuthenticatedUserResponse;
import com.smartcity.springservice.api.dto.IncidentResponse;
import com.smartcity.springservice.domain.core.enums.IncidentStatus;
import com.smartcity.springservice.domain.core.enums.IncidentType;
import com.smartcity.springservice.domain.core.enums.PriorityLevel;
import com.smartcity.springservice.domain.core.enums.UserRole;
import com.smartcity.springservice.service.CurrentUserService;
import com.smartcity.springservice.service.IncidentService;

@SpringBootTest
@AutoConfigureMockMvc
class IncidentControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private IncidentService incidentService;

	@MockBean
	private CurrentUserService currentUserService;

	@Test
	void createIncident_validRequest_returns201() throws Exception {
		IncidentResponse response = new IncidentResponse(
			UUID.randomUUID(), "Power outage", null,
			PriorityLevel.HIGH, IncidentType.OTHER, IncidentStatus.ACTIVE,
			Instant.now(), Instant.now(), null
		);
		when(incidentService.createIncident(any())).thenReturn(response);

		mockMvc.perform(post("/api/incidents")
				.with(authenticatedAs(UserRole.CITIZEN))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"Power outage\",\"priority\":\"HIGH\",\"type\":\"OTHER\",\"latitude\":41.9981,\"longitude\":21.4254}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.title").value("Power outage"))
			.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	@Test
	void createIncident_missingTitle_returns422() throws Exception {
		mockMvc.perform(post("/api/incidents")
				.with(authenticatedAs(UserRole.CITIZEN))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"priority\":\"HIGH\",\"type\":\"OTHER\",\"latitude\":41.9981,\"longitude\":21.4254}"))
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void createIncident_missingLatitude_returns422() throws Exception {
		mockMvc.perform(post("/api/incidents")
				.with(authenticatedAs(UserRole.CITIZEN))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"Power outage\",\"priority\":\"HIGH\",\"type\":\"OTHER\",\"longitude\":21.4254}"))
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void listIncidents_requiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/incidents"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void listIncidents_returns200WithList() throws Exception {
		IncidentResponse r1 = new IncidentResponse(
			UUID.randomUUID(), "Incident 1", null,
			PriorityLevel.HIGH, IncidentType.FIRE, IncidentStatus.ACTIVE,
			Instant.now(), Instant.now(), null
		);
		IncidentResponse r2 = new IncidentResponse(
			UUID.randomUUID(), "Incident 2", null,
			PriorityLevel.LOW, IncidentType.OTHER, IncidentStatus.RESOLVED,
			Instant.now(), Instant.now(), Instant.now()
		);
		when(incidentService.listIncidents(isNull(), isNull())).thenReturn(List.of(r1, r2));

		mockMvc.perform(get("/api/incidents").with(authenticatedAs(UserRole.AUTHORITY)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void listIncidents_filterByStatus_returnsOnlyActive() throws Exception {
		IncidentResponse active = new IncidentResponse(
			UUID.randomUUID(), "Active incident", null,
			PriorityLevel.HIGH, IncidentType.FIRE, IncidentStatus.ACTIVE,
			Instant.now(), Instant.now(), null
		);
		when(incidentService.listIncidents(eq(IncidentStatus.ACTIVE), isNull()))
			.thenReturn(List.of(active));

		mockMvc.perform(
			get("/api/incidents")
				.param("status", "ACTIVE")
				.with(authenticatedAs(UserRole.CITIZEN))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].status").value("ACTIVE"));
	}

	@Test
	void listIncidents_filterByPriority_returnsOnlyHigh() throws Exception {
		IncidentResponse highPriority = new IncidentResponse(
			UUID.randomUUID(), "High priority incident", null,
			PriorityLevel.HIGH, IncidentType.ACCIDENT, IncidentStatus.ACTIVE,
			Instant.now(), Instant.now(), null
		);
		when(incidentService.listIncidents(isNull(), eq(PriorityLevel.HIGH)))
			.thenReturn(List.of(highPriority));

		mockMvc.perform(
			get("/api/incidents")
				.param("priority", "HIGH")
				.with(authenticatedAs(UserRole.OPERATOR))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].priority").value("HIGH"));
	}

	@Test
	void getIncident_returns200ForAdmin() throws Exception {
		UUID id = UUID.randomUUID();
		IncidentResponse response = new IncidentResponse(
			id, "Incident details", null,
			PriorityLevel.MEDIUM, IncidentType.OTHER, IncidentStatus.ACTIVE,
			Instant.now(), Instant.now(), null
		);
		when(incidentService.getIncident(id)).thenReturn(response);

		mockMvc.perform(get("/api/incidents/" + id).with(authenticatedAs(UserRole.ADMIN)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id.toString()))
			.andExpect(jsonPath("$.title").value("Incident details"));
	}

	@Test
	void updateIncident_statusToResolved_returns200WithResolvedAt() throws Exception {
		UUID id = UUID.randomUUID();
		IncidentResponse resolved = new IncidentResponse(
			id, "Some incident", null,
			PriorityLevel.MEDIUM, IncidentType.OTHER, IncidentStatus.RESOLVED,
			Instant.now(), Instant.now(), Instant.now()
		);
		when(incidentService.updateIncident(eq(id), any())).thenReturn(resolved);

		mockMvc.perform(patch("/api/incidents/" + id)
				.with(authenticatedAs(UserRole.OPERATOR))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\":\"RESOLVED\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("RESOLVED"))
			.andExpect(jsonPath("$.resolvedAt").isNotEmpty());
	}

	@Test
	void updateIncident_rejectsCitizenRole() throws Exception {
		UUID id = UUID.randomUUID();

		mockMvc.perform(patch("/api/incidents/" + id)
				.with(authenticatedAs(UserRole.CITIZEN))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\":\"RESOLVED\"}"))
			.andExpect(status().isForbidden());
	}

	@Test
	void updateIncident_resolvedToActive_returns409() throws Exception {
		UUID id = UUID.randomUUID();
		when(incidentService.updateIncident(eq(id), any()))
			.thenThrow(new IllegalStateException("A resolved incident cannot be re-opened."));

		mockMvc.perform(patch("/api/incidents/" + id)
				.with(authenticatedAs(UserRole.ADMIN))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\":\"ACTIVE\"}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error").value("CONFLICT"))
			.andExpect(jsonPath("$.message").value("A resolved incident cannot be re-opened."));
	}

	@Test
	void getIncident_nonExistentId_returns404() throws Exception {
		UUID nonExistentId = UUID.fromString("00000000-0000-0000-0000-000000000000");
		when(incidentService.getIncident(nonExistentId)).thenThrow(new NoSuchElementException());

		mockMvc.perform(get("/api/incidents/" + nonExistentId).with(authenticatedAs(UserRole.CITIZEN)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("NOT_FOUND"));
	}

	private RequestPostProcessor authenticatedAs(UserRole role) {
		String clerkUserId = "user-" + role.name().toLowerCase();
		when(currentUserService.resolveActiveUser(clerkUserId)).thenReturn(userWithRole(clerkUserId, role));

		return jwt().jwt((jwt) -> jwt.subject(clerkUserId).claim("email", clerkUserId + "@example.com"));
	}

	private AuthenticatedUserResponse userWithRole(String clerkUserId, UserRole role) {
		return new AuthenticatedUserResponse(
			UUID.randomUUID(),
			clerkUserId,
			clerkUserId + "@example.com",
			"Test " + role.name(),
			role,
			null,
			true
		);
	}
}
