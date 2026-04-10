package com.smartcity.springservice.domain.core.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcity.springservice.domain.core.entity.IncidentStatusHistory;

public interface IncidentStatusHistoryRepository extends JpaRepository<IncidentStatusHistory, UUID> {
}
