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
	@Column(name = "old_status", length = 16)
	private EventStatus oldStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "new_status", nullable = false, length = 16)
	private EventStatus newStatus;

	@Column(name = "note")
	private String note;

	public void setEvent(CityEvent event) { this.event = event; }
	public void setChangedByUser(UserProfile user) { this.changedByUser = user; }
	public void setOldStatus(EventStatus oldStatus) { this.oldStatus = oldStatus; }
	public void setNewStatus(EventStatus newStatus) { this.newStatus = newStatus; }
	public void setNote(String note) { this.note = note; }
}
