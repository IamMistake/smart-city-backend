package com.smartcity.springservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IncidentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void createIncident_withValidBody_returns201() throws Exception {
		String body = """
				{
				  "title": "Flooding near bridge",
				  "description": "Rising water levels",
				  "priority": "HIGH"
				}
				""";

		mockMvc.perform(post("/api/incidents")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNotEmpty())
			.andExpect(jsonPath("$.title").value("Flooding near bridge"))
			.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	@Test
	void createIncident_withMissingTitle_returns422() throws Exception {
		String body = """
				{
				  "priority": "LOW"
				}
				""";

		mockMvc.perform(post("/api/incidents")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isUnprocessableEntity())
			.andExpect(jsonPath("$.error").value("Validation failed"))
			.andExpect(jsonPath("$.status").value(422));
	}

	@Test
	void listIncidents_returns200WithList() throws Exception {
		mockMvc.perform(get("/api/incidents"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$", not(empty())));
	}

	@Test
	void listIncidents_filterByStatusActive_returnsOnlyActive() throws Exception {
		mockMvc.perform(get("/api/incidents").param("status", "ACTIVE"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[*].status", everyItem(is("ACTIVE"))));
	}

	@Test
	void listIncidents_filterByPriorityHigh_returnsOnlyHigh() throws Exception {
		mockMvc.perform(get("/api/incidents").param("priority", "HIGH"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[*].priority", everyItem(is("HIGH"))));
	}

	@Test
	void patchIncident_toResolved_setsResolvedAt() throws Exception {
		String createBody = """
				{
				  "title": "Sensor offline",
				  "priority": "MEDIUM"
				}
				""";

		MvcResult created = mockMvc.perform(post("/api/incidents")
				.contentType(MediaType.APPLICATION_JSON)
				.content(createBody))
			.andExpect(status().isCreated())
			.andReturn();

		Integer id = objectMapper.readTree(created.getResponse().getContentAsString())
			.get("id")
			.asInt();

		String patchBody = """
				{
				  "status": "RESOLVED"
				}
				""";

		mockMvc.perform(patch("/api/incidents/" + id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(patchBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("RESOLVED"))
			.andExpect(jsonPath("$.resolvedAt").isNotEmpty());
	}

	@Test
	void getIncident_withNonExistentId_returns404() throws Exception {
		mockMvc.perform(get("/api/incidents/9999"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("Not found"))
			.andExpect(jsonPath("$.status").value(404));
	}
}
