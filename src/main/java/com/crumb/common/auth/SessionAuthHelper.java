package com.crumb.common.auth;

import com.crumb.common.exception.ForbiddenException;
import com.crumb.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpSession;

/**
 * SessionAuthHelper - Utility class for session-based authentication and authorization.
 * 
 * WHY: Provides centralized methods to check user login status, roles (CUSTOMER vs ADMIN),
 * and enforce security boundaries across all controllers.
 */
public class SessionAuthHelper {

    public static final String SESSION_USER_ID = "CRUMB_USER_ID";
    public static final String SESSION_USER_NAME = "CRUMB_USER_NAME";
    public static final String SESSION_USER_EMAIL = "CRUMB_USER_EMAIL";
    public static final String SESSION_USER_ROLE = "CRUMB_USER_ROLE";

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    /**
     * Store authenticated user details into HTTP session.
     */
    public static void loginUser(HttpSession session, Long userId, String name, String email, String role) {
        session.setAttribute(SESSION_USER_ID, userId);
        session.setAttribute(SESSION_USER_NAME, name);
        session.setAttribute(SESSION_USER_EMAIL, email);
        session.setAttribute(SESSION_USER_ROLE, role);
    }

    /**
     * Clear user details from HTTP session on logout.
     */
    public static void logoutUser(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Check if a user is currently logged in.
     */
    public static boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute(SESSION_USER_ID) != null;
    }

    /**
     * Check if the logged-in user is an ADMIN.
     */
    public static boolean isAdmin(HttpSession session) {
        return isLoggedIn(session) && ROLE_ADMIN.equalsIgnoreCase((String) session.getAttribute(SESSION_USER_ROLE));
    }

    /**
     * Get current user ID from session. Returns null if not logged in.
     */
    public static Long getCurrentUserId(HttpSession session) {
        if (session == null) return null;
        Object id = session.getAttribute(SESSION_USER_ID);
        return id != null ? (Long) id : null;
    }

    /**
     * Get current user name from session.
     */
    public static String getCurrentUserName(HttpSession session) {
        return session != null ? (String) session.getAttribute(SESSION_USER_NAME) : null;
    }

    /**
     * Get current user role from session.
     */
    public static String getCurrentUserRole(HttpSession session) {
        return session != null ? (String) session.getAttribute(SESSION_USER_ROLE) : null;
    }

    /**
     * Require that user is logged in; throws UnauthorizedException otherwise.
     */
    public static Long requireLoggedIn(HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            throw new UnauthorizedException("You must be logged in to perform this action.");
        }
        return userId;
    }

    /**
     * Require that user is an administrator; throws ForbiddenException or UnauthorizedException.
     */
    public static void requireAdmin(HttpSession session) {
        if (!isLoggedIn(session)) {
            throw new UnauthorizedException("Please log in with an administrator account.");
        }
        if (!isAdmin(session)) {
            throw new ForbiddenException("Access denied: Administrator privileges required.");
        }
    }
}
