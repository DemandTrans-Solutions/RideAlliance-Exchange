package com.clearinghouse.service;

import com.clearinghouse.dao.UserDAO;
import com.clearinghouse.dto.UserContextDTO;
import com.clearinghouse.entity.UserAuthentication;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service for extracting and providing user context information from the current security context.
 * <p>
 * This service retrieves the authenticated user's details, including their provider ID, role, and user ID,
 * and packages them into a {@link UserContextDTO}. It handles cases where the user or provider information
 * may be missing or needs to be re-fetched from the database.
 * </p>
 */
@Service
@AllArgsConstructor
public class UserContextService {

    /**
     * Data access object for user-related database operations.
     */
    private final UserDAO userDAO;

    /**
     * Extracts the current user's context from the Spring Security context.
     * <p>
     * This method retrieves the authenticated user's role, user ID, and provider ID. If the user or provider
     * information is missing from the authentication object, it attempts to reload the user from the database.
     * </p>
     *
     * @return a {@link UserContextDTO} containing the provider ID, user role, and user ID. If authentication is missing,
     * returns a DTO with default values.
     */
    public UserContextDTO extractUserContext() {
        var authentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new UserContextDTO(0, "", 0);
        }

        String userRole = authentication.getAuthorities().iterator().next().getAuthority();
        var user = authentication.getAuthenticatedUser();
        if (user == null) {
            return new UserContextDTO(0, userRole, 0);
        }

        if (user.getProvider() == null) {
            user = userDAO.findUserByUserId(user.getId());
        }

        int providerId = user.getProvider().getProviderId();
        return new UserContextDTO(providerId, userRole, user.getId());
    }

}
