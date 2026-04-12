package com.smartcity.springservice.domain.core.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcity.springservice.domain.core.entity.Camera;

public interface CameraRepository extends JpaRepository<Camera, UUID> {
}
