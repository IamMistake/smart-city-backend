package com.smartcity.springservice.repository;

import com.smartcity.springservice.domain.core.entity.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CameraRepository extends JpaRepository<Camera, UUID> {
}