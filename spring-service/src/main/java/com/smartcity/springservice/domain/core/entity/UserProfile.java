package com.smartcity.springservice.domain.core.entity;

import com.smartcity.springservice.domain.common.SoftDeleteModel;
import com.smartcity.springservice.domain.core.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

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

	public void setClerkUserId(String id) { this.clerkUserId = id; }
	public void setEmail(String email) { this.email = email; }
	public void setFullName(String name) { this.fullName = name; }
	public void setRole(UserRole role) { this.role = role; }
	public void setAvatarUrl(String url) { this.avatarUrl = url; }
	public void setActive(boolean active) { this.isActive = active; }
}
