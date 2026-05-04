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
	@org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
	@Column(name = "role", nullable = false)
	private UserRole role;

	@Column(name = "avatar_url")
	private String avatarUrl;

	@Column(name = "is_active", nullable = false)
	private boolean isActive = true;

	public String getClerkUserId() { return clerkUserId; }

	public void setClerkUserId(String clerkUserId) { this.clerkUserId = clerkUserId; }

	public String getEmail() { return email; }

	public void setEmail(String email) { this.email = email; }

	public String getFullName() { return fullName; }

	public void setFullName(String fullName) { this.fullName = fullName; }

	public UserRole getRole() { return role; }

	public void setRole(UserRole role) { this.role = role; }

	public String getAvatarUrl() { return avatarUrl; }

	public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

	public boolean isActive() { return isActive; }

	public void setActive(boolean active) { isActive = active; }
}
