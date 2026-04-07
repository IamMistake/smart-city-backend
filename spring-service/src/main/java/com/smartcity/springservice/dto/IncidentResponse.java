package com.smartcity.springservice.dto;

import com.smartcity.springservice.model.IncidentPriority;
import com.smartcity.springservice.model.IncidentStatus;
import java.time.LocalDateTime;

public class IncidentResponse {

	private Long id;
	private String title;
	private String description;
	private IncidentPriority priority;
	private IncidentStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime resolvedAt;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public IncidentPriority getPriority() { return priority; }
	public void setPriority(IncidentPriority priority) { this.priority = priority; }

	public IncidentStatus getStatus() { return status; }
	public void setStatus(IncidentStatus status) { this.status = status; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

	public LocalDateTime getResolvedAt() { return resolvedAt; }
	public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
