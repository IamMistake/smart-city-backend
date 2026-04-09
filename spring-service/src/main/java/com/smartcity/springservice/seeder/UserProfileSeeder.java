package com.smartcity.springservice.seeder;

import com.smartcity.springservice.domain.core.entity.UserProfile;
import com.smartcity.springservice.domain.core.enums.UserRole;
import com.smartcity.springservice.repository.UserProfileRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("demo")
public class UserProfileSeeder {

    private final UserProfileRepository userProfileRepository;

    public UserProfileSeeder(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public void seed() {
        if (userProfileRepository.count() > 0) {
            System.out.println(">>> [DEMO] Users already seeded, skipping.");
            return;
        }

        List<UserProfile> users = List.of(
                buildUser("demo_clerk_001", "admin@smartcity.mk",    "Demo Admin",    UserRole.ADMIN),
                buildUser("demo_clerk_002", "operator@smartcity.mk", "Demo Operator", UserRole.OPERATOR),
                buildUser("demo_clerk_003", "citizen1@smartcity.mk", "Ana Petrovska", UserRole.CITIZEN),
                buildUser("demo_clerk_004", "citizen2@smartcity.mk", "Marko Ilievski",UserRole.CITIZEN),
                buildUser("demo_clerk_005", "authority@smartcity.mk","City Authority", UserRole.AUTHORITY)
        );

        userProfileRepository.saveAll(users);
        System.out.println(">>> [DEMO] Seeded " + users.size() + " users.");
    }

    private UserProfile buildUser(String clerkId, String email, String fullName, UserRole role) {
        UserProfile u = new UserProfile();
        u.setClerkUserId(clerkId);
        u.setEmail(email);
        u.setFullName(fullName);
        u.setRole(role);
        u.setActive(true);
        return u;
    }
}