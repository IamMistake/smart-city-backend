package com.smartcity.springservice.repository;

import com.smartcity.springservice.model.Incident;
import com.smartcity.springservice.model.IncidentPriority;
import com.smartcity.springservice.model.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
	List<Incident> findByStatus(IncidentStatus status);
	List<Incident> findByPriority(IncidentPriority priority);
	List<Incident> findByStatusAndPriority(IncidentStatus status, IncidentPriority priority);
}
