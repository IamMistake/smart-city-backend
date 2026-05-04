package com.smartcity.springservice.domain.common;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class CreationAwareModel extends BaseModel {
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public Instant getCreatedAt() { return createdAt; }

	public Instant getUpdatedAt() { return updatedAt; }

	public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

	public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
