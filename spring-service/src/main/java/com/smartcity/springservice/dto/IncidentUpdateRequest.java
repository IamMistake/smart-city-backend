package com.smartcity.springservice.dto;

import com.smartcity.springservice.model.IncidentPriority;
import com.smartcity.springservice.model.IncidentStatus;

public class IncidentUpdateRequest {

	private IncidentStatus status;
	private IncidentPriority priority;

	public IncidentStatus getStatus() { return status; }
	public void setStatus(IncidentStatus status) { this.status = status; }

	public IncidentPriority getPriority() { return priority; }
	public void setPriority(IncidentPriority priority) { this.priority = priority; }
}
