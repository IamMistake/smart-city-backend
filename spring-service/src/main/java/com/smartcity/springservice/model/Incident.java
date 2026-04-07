package com.smartcity.springservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
public class Incident {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private IncidentPriority priority;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private IncidentStatus status = IncidentStatus.ACTIVE;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	private LocalDateTime resolvedAt;

	@PrePersist
	private void prePersist() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	private void preUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public Long getId() { return id; }

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public IncidentPriority getPriority() { return priority; }
	public void setPriority(IncidentPriority priority) { this.priority = priority; }

	public IncidentStatus getStatus() { return status; }
	public void setStatus(IncidentStatus status) { this.status = status; }

	public LocalDateTime getCreatedAt() { return createdAt; }

	public LocalDateTime getUpdatedAt() { return updatedAt; }

	public LocalDateTime getResolvedAt() { return resolvedAt; }
	public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
