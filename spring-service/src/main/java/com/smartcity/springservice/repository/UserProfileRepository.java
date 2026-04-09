package com.smartcity.springservice.repository;

import com.smartcity.springservice.domain.core.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByEmail(String email);
}