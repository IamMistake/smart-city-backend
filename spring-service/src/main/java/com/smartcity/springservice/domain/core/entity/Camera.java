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
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
	@Column(name = "status", nullable = false, length = 16)
	private CameraStatus status;

	@Column(name = "stream_url")
	private String streamUrl;
}
