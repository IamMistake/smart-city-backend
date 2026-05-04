package com.smartcity.springservice.domain.core.entity;

import com.smartcity.springservice.domain.common.CreationAwareModel;
import com.smartcity.springservice.domain.core.enums.EventStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "core", name = "event_status_history")
public class EventStatusHistory extends CreationAwareModel {
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "event_id", nullable = false)
	private CityEvent event;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "changed_by_user_id")
	private UserProfile changedByUser;

	@Enumerated(EnumType.STRING)
	@org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
	@Column(name = "old_status")
	private EventStatus oldStatus;

	@Enumerated(EnumType.STRING)
	@org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
	@Column(name = "new_status", nullable = false)
	private EventStatus newStatus;

	@Column(name = "note")
	private String note;

	public CityEvent getEvent() { return event; }

	public void setEvent(CityEvent event) { this.event = event; }

	public UserProfile getChangedByUser() { return changedByUser; }

	public void setChangedByUser(UserProfile changedByUser) { this.changedByUser = changedByUser; }

	public EventStatus getOldStatus() { return oldStatus; }

	public void setOldStatus(EventStatus oldStatus) { this.oldStatus = oldStatus; }

	public EventStatus getNewStatus() { return newStatus; }

	public void setNewStatus(EventStatus newStatus) { this.newStatus = newStatus; }

	public String getNote() { return note; }

	public void setNote(String note) { this.note = note; }
}
