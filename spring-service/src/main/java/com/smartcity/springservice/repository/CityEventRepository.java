package com.smartcity.springservice.repository;

import com.smartcity.springservice.domain.core.entity.CityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CityEventRepository extends JpaRepository<CityEvent, UUID> {
}