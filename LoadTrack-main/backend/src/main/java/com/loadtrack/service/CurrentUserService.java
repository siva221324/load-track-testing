package com.loadtrack.service;

import com.loadtrack.entity.User;
import com.loadtrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("No authenticated user in security context");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }

    public Long getCurrentDriverId() {
        User u = getCurrentUser();
        if (u.getLinkedDriver() == null) {
            throw new IllegalStateException(
                    "Your account is not linked to a driver. Ask the admin to link your login.");
        }
        return u.getLinkedDriver().getId();
    }

    public Long getCurrentDealerId() {
        User u = getCurrentUser();
        if (u.getLinkedDealer() == null) {
            throw new IllegalStateException(
                    "Your account is not linked to a dealer. Ask the admin to link your login.");
        }
        return u.getLinkedDealer().getId();
    }

    /**
     * Returns the organization (tenant) ID for the current user.
     * ADMIN: from user.organization
     * DRIVER: from user.linkedDriver.organization
     * DEALER: from user.linkedDealer.organization
     */
    public Long getCurrentOrgId() {
        User u = getCurrentUser();
        if (u.getOrganization() != null) {
            return u.getOrganization().getId();
        }
        if (u.getLinkedDriver() != null && u.getLinkedDriver().getOrganization() != null) {
            return u.getLinkedDriver().getOrganization().getId();
        }
        if (u.getLinkedDealer() != null && u.getLinkedDealer().getOrganization() != null) {
            return u.getLinkedDealer().getOrganization().getId();
        }
        throw new IllegalStateException(
                "User '" + u.getUsername() + "' is not assigned to any organization");
    }
}
