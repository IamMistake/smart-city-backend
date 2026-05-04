package com.smartcity.springservice.domain.core.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.smartcity.springservice.domain.common.SoftDeleteModel;
import com.smartcity.springservice.domain.core.enums.IncidentStatus;
import com.smartcity.springservice.domain.core.enums.IncidentType;
import com.smartcity.springservice.domain.core.enums.PriorityLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "core", name = "incidents")
public class Incident extends SoftDeleteModel {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reported_by_user_id")
	private UserProfile reportedByUser;

    @Column(name = "title", nullable = false, length = 255)
	private String title;

    @Column(name = "description")
	private String description;

	@Enumerated(EnumType.STRING)
	@org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
	@Column(name = "incident_type", nullable = false)
	private IncidentType incidentType;

	@Enumerated(EnumType.STRING)
	@org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
	@Column(name = "priority", nullable = false)
	private PriorityLevel priority;

	@Enumerated(EnumType.STRING)
	@org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
	@Column(name = "status", nullable = false)
	private IncidentStatus status = IncidentStatus.ACTIVE;

	@Column(name = "latitude", precision = 9, scale = 6)
	private BigDecimal latitude;

	@Column(name = "longitude", precision = 9, scale = 6)
	private BigDecimal longitude;

	@Column(name = "address", length = 255)
	private String address;

	@Column(name = "occurred_at")
	private Instant occurredAt;

	@Column(name = "resolved_at")
	private Instant resolvedAt;

	public UserProfile getReportedByUser() { return reportedByUser; }

	public void setReportedByUser(UserProfile reportedByUser) { this.reportedByUser = reportedByUser; }

	public String getTitle() { return title; }

	public void setTitle(String title) { this.title = title; }

	public String getDescription() { return description; }

	public void setDescription(String description) { this.description = description; }

	public IncidentType getIncidentType() { return incidentType; }

	public void setIncidentType(IncidentType incidentType) { this.incidentType = incidentType; }

	public PriorityLevel getPriority() { return priority; }

	public void setPriority(PriorityLevel priority) { this.priority = priority; }

	public IncidentStatus getStatus() { return status; }

	public void setStatus(IncidentStatus status) { this.status = status; }

	public BigDecimal getLatitude() { return latitude; }

	public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

	public BigDecimal getLongitude() { return longitude; }

	public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

	public String getAddress() { return address; }

	public void setAddress(String address) { this.address = address; }

	public Instant getOccurredAt() { return occurredAt; }

	public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

	public Instant getResolvedAt() { return resolvedAt; }

	public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
}
