package com.smartcity.springservice.repository;

import com.smartcity.springservice.domain.core.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {
}