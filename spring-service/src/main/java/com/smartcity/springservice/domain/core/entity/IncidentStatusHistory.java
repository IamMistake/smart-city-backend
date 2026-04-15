package com.smartcity.springservice.domain.core.entity;

import com.smartcity.springservice.domain.common.CreationAwareModel;
import com.smartcity.springservice.domain.core.enums.IncidentStatus;

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
@Table(schema = "core", name = "incident_status_history")
public class IncidentStatusHistory extends CreationAwareModel {
	@Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "incident_id", nullable = false)
	private Incident incident;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "changed_by_user_id")
	private UserProfile changedByUser;

	@Setter
    @Enumerated(EnumType.STRING)
	@Column(name = "old_status", length = 16)
	private IncidentStatus oldStatus;

	@Setter
    @Enumerated(EnumType.STRING)
	@Column(name = "new_status", nullable = false, length = 16)
	private IncidentStatus newStatus;

	@Column(name = "note")
	private String note;

}
