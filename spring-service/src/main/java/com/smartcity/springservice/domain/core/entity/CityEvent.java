package com.smartcity.springservice.domain.core.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.smartcity.springservice.domain.common.SoftDeleteModel;
import com.smartcity.springservice.domain.core.enums.EventStatus;
import com.smartcity.springservice.domain.core.enums.EventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "core", name = "events")
public class CityEvent extends SoftDeleteModel {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by_user_id")
	private UserProfile createdByUser;

	@Column(name = "title", nullable = false, length = 255)
	private String title;

	@Column(name = "description")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 32)
	private EventType eventType;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 16)
	private EventStatus status;

	@Column(name = "latitude", nullable = false, precision = 9, scale = 6)
	private BigDecimal latitude;

	@Column(name = "longitude", nullable = false, precision = 9, scale = 6)
	private BigDecimal longitude;

	@Column(name = "address", length = 255)
	private String address;

	@Column(name = "start_time")
	private Instant startTime;

	@Column(name = "end_time")
	private Instant endTime;

	public void setCreatedByUser(UserProfile user) { this.createdByUser = user; }
	public void setTitle(String title) { this.title = title; }
	public void setDescription(String description) { this.description = description; }
	public void setEventType(EventType eventType) { this.eventType = eventType; }
	public void setStatus(EventStatus status) { this.status = status; }
	public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
	public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
	public void setAddress(String address) { this.address = address; }
	public void setStartTime(Instant startTime) { this.startTime = startTime; }
	public void setEndTime(Instant endTime) { this.endTime = endTime; }
}
