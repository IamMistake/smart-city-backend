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

@Entity
@Table(schema = "core", name = "police_units")
public class PoliceUnit extends SoftDeleteModel {
	@Column(name = "unit_code", nullable = false, unique = true, length = 64)
	private String unitCode;

	@Column(name = "display_name", length = 255)
	private String displayName;

	@Enumerated(EnumType.STRING)
	@org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
	@Column(name = "status", nullable = false)
	private PoliceUnitStatus status;

	@Column(name = "current_latitude", precision = 9, scale = 6)
	private BigDecimal currentLatitude;

	@Column(name = "current_longitude", precision = 9, scale = 6)
	private BigDecimal currentLongitude;

	@Column(name = "last_reported_at")
	private Instant lastReportedAt;

	public String getUnitCode() { return unitCode; }

	public void setUnitCode(String unitCode) { this.unitCode = unitCode; }

	public String getDisplayName() { return displayName; }

	public void setDisplayName(String displayName) { this.displayName = displayName; }

	public PoliceUnitStatus getStatus() { return status; }

	public void setStatus(PoliceUnitStatus status) { this.status = status; }

	public BigDecimal getCurrentLatitude() { return currentLatitude; }

	public void setCurrentLatitude(BigDecimal currentLatitude) { this.currentLatitude = currentLatitude; }

	public BigDecimal getCurrentLongitude() { return currentLongitude; }

	public void setCurrentLongitude(BigDecimal currentLongitude) { this.currentLongitude = currentLongitude; }

	public Instant getLastReportedAt() { return lastReportedAt; }

	public void setLastReportedAt(Instant lastReportedAt) { this.lastReportedAt = lastReportedAt; }
}
