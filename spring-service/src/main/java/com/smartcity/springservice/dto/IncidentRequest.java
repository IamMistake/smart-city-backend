package com.smartcity.springservice.dto;

import com.smartcity.springservice.model.IncidentPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class IncidentRequest {

	@NotBlank
	private String title;

	private String description;

	@NotNull
	private IncidentPriority priority;

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public IncidentPriority getPriority() { return priority; }
	public void setPriority(IncidentPriority priority) { this.priority = priority; }
}
