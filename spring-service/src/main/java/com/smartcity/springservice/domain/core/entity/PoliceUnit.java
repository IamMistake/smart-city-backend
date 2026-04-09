package com.smartcity.springservice.domain.core.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.smartcity.springservice.domain.common.SoftDeleteModel;
import com.smartcity.springservice.domain.core.enums.PoliceUnitStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(schema = "core", name = "police_units")
public class PoliceUnit extends SoftDeleteModel {
	@Column(name = "unit_code", nullable = false, unique = true, length = 64)
	private String unitCode;

	@Column(name = "display_name", length = 255)
	private String displayName;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 16)
	private PoliceUnitStatus status;

	@Column(name = "current_latitude", precision = 9, scale = 6)
	private BigDecimal currentLatitude;

	@Column(name = "current_longitude", precision = 9, scale = 6)
	private BigDecimal currentLongitude;

	@Column(name = "last_reported_at")
	private Instant lastReportedAt;
}
