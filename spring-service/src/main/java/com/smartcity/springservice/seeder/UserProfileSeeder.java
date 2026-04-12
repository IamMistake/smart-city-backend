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
        if (userProfileRepository.count() > 0) {
            System.out.println(">>> [DEMO] UserProfiles already seeded, skipping.");
            return;
        }

        userProfileRepository.save(createUser("demo_clerk_001", "admin@smartcity.mk", "Admin User", UserRole.ADMIN));
        userProfileRepository.save(createUser("demo_clerk_002", "operator@smartcity.mk", "Operator User", UserRole.OPERATOR));
        userProfileRepository.save(createUser("demo_clerk_003", "citizen1@smartcity.mk", "Citizen One", UserRole.CITIZEN));
        userProfileRepository.save(createUser("demo_clerk_004", "citizen2@smartcity.mk", "Citizen Two", UserRole.CITIZEN));
        userProfileRepository.save(createUser("demo_clerk_005", "authority@smartcity.mk", "Authority User", UserRole.AUTHORITY));

        System.out.println(">>> [DEMO] Seeded 5 user profiles.");
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