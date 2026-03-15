/*
 * FILE: AuthInterceptor.java
 * PURPOSE: Spring MVC interceptor that enforces session-based authentication for
 *          backend API routes. It is registered by WebConfig and runs before controllers.
 *          Any unauthenticated request to /api/** is rejected with HTTP 401 JSON.
 *
 * METHODS:
 *  - preHandle(request, response, handler)
 *      Authentication gate executed before controller handlers.
 *      Logic:
 *        1. Allow all OPTIONS requests for CORS preflight.
 *        2. Allow /api/auth/** so login/logout/me can work without prior auth.
 *        3. Ignore non-/api/ paths.
 *        4. For protected API paths, require a session containing userId.
 *        5. If missing, return 401 with {"error":"Not authenticated"} and stop.
 *
 * HOW TO MODIFY:
 *  - To allow additional public API routes: add extra path checks before the
 *    session validation block.
 *  - To enforce role-based authorization globally: read session.userRole here and
 *    reject specific path prefixes based on role.
 *  - To move to token/JWT auth: replace the HttpSession check with Authorization
 *    header parsing and token validation.
 */
package com.example.demo.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String path = request.getRequestURI();

        // Allow OPTIONS (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        // Skip auth for the login/logout endpoints themselves
        if (path.startsWith("/api/auth/")) return true;

        // Only protect /api/** paths
        if (!path.startsWith("/api/")) return true;

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Not authenticated\"}");
            return false;
        }
        return true;
    }
}
