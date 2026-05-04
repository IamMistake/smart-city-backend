package com.smartcity.springservice.domain.common;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class SoftDeleteModel extends CreationAwareModel {
	@Column(name = "deleted_at")
	private Instant deletedAt;

	public Instant getDeletedAt() { return deletedAt; }

	public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}
