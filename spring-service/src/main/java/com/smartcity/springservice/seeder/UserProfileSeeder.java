package com.smartcity.springservice.seeder;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.smartcity.springservice.domain.core.entity.UserProfile;
import com.smartcity.springservice.domain.core.enums.UserRole;
import com.smartcity.springservice.domain.core.repository.UserProfileRepository;

@Component
@Profile("demo")
public class UserProfileSeeder {

    private final UserProfileRepository userProfileRepository;

    public UserProfileSeeder(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

	public void seed() {
		upsertUser("demo_clerk_001", "admin@smartcity.mk", "Admin User", UserRole.ADMIN);
		upsertUser("demo_clerk_002", "operator@smartcity.mk", "Operator User", UserRole.OPERATOR);
		upsertUser("demo_clerk_003", "citizen1@smartcity.mk", "Citizen One", UserRole.CITIZEN);
		upsertUser("demo_clerk_004", "citizen2@smartcity.mk", "Citizen Two", UserRole.CITIZEN);
		upsertUser("demo_clerk_005", "authority@smartcity.mk", "Authority User", UserRole.AUTHORITY);

		System.out.println(">>> [DEMO] Ensured 5 demo user profiles exist.");
	}

	private void upsertUser(String clerkUserId, String email, String fullName, UserRole role) {
		UserProfile user = userProfileRepository
			.findByClerkUserIdAndDeletedAtIsNull(clerkUserId)
			.orElseGet(UserProfile::new);

		user.setClerkUserId(clerkUserId);
		user.setEmail(email);
		user.setFullName(fullName);
		user.setRole(role);
		user.setActive(true);

		userProfileRepository.save(user);
	}

	private UserProfile createUser(String clerkUserId, String email, String fullName, UserRole role) {
		UserProfile user = new UserProfile();
        user.setClerkUserId(clerkUserId);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(role);
        user.setActive(true);
        return user;
    }
}
