package com.smartcity.springservice.domain.common;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeleteModel extends CreationAwareModel {
	@Column(name = "deleted_at")
	private Instant deletedAt;
}
