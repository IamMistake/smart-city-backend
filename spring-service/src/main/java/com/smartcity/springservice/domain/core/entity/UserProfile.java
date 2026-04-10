package com.smartcity.springservice.domain.core.entity;

import com.smartcity.springservice.domain.common.SoftDeleteModel;
import com.smartcity.springservice.domain.core.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(schema = "core", name = "user_profiles")
public class UserProfile extends SoftDeleteModel {
	@Column(name = "clerk_user_id", nullable = false, unique = true, length = 255)
	private String clerkUserId;

	@Column(name = "email", nullable = false, unique = true, length = 320)
	private String email;

	@Column(name = "full_name", length = 255)
	private String fullName;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 32)
	private UserRole role;

	@Column(name = "avatar_url")
	private String avatarUrl;

	@Column(name = "is_active", nullable = false)
	private boolean isActive = true;
}
