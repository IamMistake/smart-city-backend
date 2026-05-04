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

@Entity
@Table(schema = "core", name = "incident_status_history")
public class IncidentStatusHistory extends CreationAwareModel {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "incident_id", nullable = false)
	private Incident incident;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "changed_by_user_id")
	private UserProfile changedByUser;

	@Enumerated(EnumType.STRING)
	@org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
	@Column(name = "old_status")
	private IncidentStatus oldStatus;

	@Enumerated(EnumType.STRING)
	@org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
	@Column(name = "new_status", nullable = false)
	private IncidentStatus newStatus;

	@Column(name = "note")
	private String note;

	public Incident getIncident() { return incident; }

	public void setIncident(Incident incident) { this.incident = incident; }

	public UserProfile getChangedByUser() { return changedByUser; }

	public void setChangedByUser(UserProfile changedByUser) { this.changedByUser = changedByUser; }

	public IncidentStatus getOldStatus() { return oldStatus; }

	public void setOldStatus(IncidentStatus oldStatus) { this.oldStatus = oldStatus; }

	public IncidentStatus getNewStatus() { return newStatus; }

	public void setNewStatus(IncidentStatus newStatus) { this.newStatus = newStatus; }

	public String getNote() { return note; }

	public void setNote(String note) { this.note = note; }
}
