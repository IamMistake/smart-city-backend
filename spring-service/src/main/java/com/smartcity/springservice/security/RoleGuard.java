package com.smartcity.springservice.security;

import com.smartcity.springservice.api.dto.AuthenticatedUserResponse;
import com.smartcity.springservice.domain.core.enums.UserRole;
import com.smartcity.springservice.service.CurrentUserService;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Arrays;

@Component("roleGuard")
@RequestScope
public class RoleGuard {

    private final CurrentUserService currentUserService;
    private AuthenticatedUserResponse cachedUser;

    public RoleGuard(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    public boolean userHasRole(String role) {
        return currentUser().role().name().equals(role);
    }

    public boolean userHasAnyRole(String... roles) {
        UserRole userRole = currentUser().role();

        return Arrays.stream(roles)
                .anyMatch(r -> userRole.name().equalsIgnoreCase(r));
    }

    public boolean userHasNoneRole(String... roles) {
        UserRole userRole = currentUser().role();

        return Arrays.stream(roles)
                .noneMatch(r -> userRole.name().equalsIgnoreCase(r));
    }

    private AuthenticatedUserResponse currentUser() {
        if (cachedUser == null) {
            Jwt jwt = (Jwt) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();

            cachedUser = currentUserService.resolveActiveUser(jwt.getSubject());
        }
        return cachedUser;
    }
}