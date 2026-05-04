package com.smartcity.springservice.domain.core.entity;

import java.math.BigDecimal;

import com.smartcity.springservice.domain.common.SoftDeleteModel;
import com.smartcity.springservice.domain.core.enums.CameraStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	schema = "core",
	name = "cameras",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_camera_provider_external", columnNames = {"provider", "external_camera_id"})
	}
)
public class Camera extends SoftDeleteModel {
	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "provider", length = 100)
	private String provider;

	@Column(name = "external_camera_id", length = 100)
	private String externalCameraId;

	@Column(name = "latitude", nullable = false, precision = 9, scale = 6)
	private BigDecimal latitude;

	@Column(name = "longitude", nullable = false, precision = 9, scale = 6)
	private BigDecimal longitude;

	@Enumerated(EnumType.STRING)
	@org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
	@Column(name = "status", nullable = false)
	private CameraStatus status;

	@Column(name = "stream_url")
	private String streamUrl;

	public String getName() { return name; }

	public void setName(String name) { this.name = name; }

	public String getProvider() { return provider; }

	public void setProvider(String provider) { this.provider = provider; }

	public String getExternalCameraId() { return externalCameraId; }

	public void setExternalCameraId(String externalCameraId) { this.externalCameraId = externalCameraId; }

	public BigDecimal getLatitude() { return latitude; }

	public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

	public BigDecimal getLongitude() { return longitude; }

	public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

	public CameraStatus getStatus() { return status; }

	public void setStatus(CameraStatus status) { this.status = status; }

	public String getStreamUrl() { return streamUrl; }

	public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }
}
