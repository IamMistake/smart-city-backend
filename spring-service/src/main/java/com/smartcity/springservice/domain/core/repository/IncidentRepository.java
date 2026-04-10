package com.smartcity.springservice.domain.core.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcity.springservice.domain.core.entity.Incident;
import com.smartcity.springservice.domain.core.enums.IncidentStatus;
import com.smartcity.springservice.domain.core.enums.PriorityLevel;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {
	List<Incident> findByStatus(IncidentStatus status);

	List<Incident> findByPriority(PriorityLevel priority);

	List<Incident> findByStatusAndPriority(IncidentStatus status, PriorityLevel priority);
}
