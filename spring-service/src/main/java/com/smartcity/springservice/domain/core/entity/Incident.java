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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
	@Column(name = "incident_type", nullable = false, length = 32)
	private IncidentType incidentType;

    @Enumerated(EnumType.STRING)
	@Column(name = "priority", nullable = false, length = 16)
	private PriorityLevel priority;

    @Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 16)
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

}
