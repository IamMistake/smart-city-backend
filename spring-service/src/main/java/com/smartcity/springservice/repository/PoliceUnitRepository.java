package com.smartcity.springservice.repository;

import com.smartcity.springservice.domain.core.entity.PoliceUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PoliceUnitRepository extends JpaRepository<PoliceUnit, UUID> {
}