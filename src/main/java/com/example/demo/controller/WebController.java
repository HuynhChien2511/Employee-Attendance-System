/*
 * FILE: WebController.java
 * PURPOSE: MVC Controller that handles page routing for the web frontend.
 *          Maps URL paths to static HTML files or performs role-based redirects.
 *          Not a REST controller — returns view names / redirects rather than JSON.
 *
 * ENDPOINTS:
 *  - GET /
 *      Root URL. Reads userRole from session and redirects to /admin, /manager,
 *      or /employee accordingly. Redirects to /login if no session.
 *
 *  - GET /login
 *      Shows the login page (login.html). If user is already logged in, redirects
 *      to / (which then routes by role).
 *
 *  - GET /admin
 *      Forwards to admin.html. Rejects non-ADMIN users to /login.
 *
 *  - GET /manager
 *      Forwards to manager.html. Accepts MANAGER and ADMIN roles only.
 *
 *  - GET /employee
 *      Forwards to employee.html. Only requires a valid session (any role).
 *
 * HOW TO MODIFY:
 *  - To add a new page/role: create the HTML file in src/main/resources/static/,
 *    add a new @GetMapping method here with the corresponding role check.
 *  - To allow ADMIN to access /employee view: add an ADMIN check in the employee()
 *    method (currently only checks if session exists, not role).
 *  - To use Thymeleaf templates instead of static HTML: place .html files in
 *    src/main/resources/templates/ and remove the "forward:" prefix — Spring MVC
 *    will resolve them via the template engine.
 */
package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class WebController {

    @GetMapping("/")
    public String index(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if (role == null) return "redirect:/login";
        return switch (role) {
            case "ADMIN" -> "redirect:/admin";
            case "MANAGER" -> "redirect:/manager";
            default -> "redirect:/employee";
        };
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        if (session.getAttribute("userRole") != null) return "redirect:/";
        return "forward:/login.html";
    }

    @GetMapping("/admin")
    public String admin(HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("userRole"))) return "redirect:/login";
        return "forward:/admin.html";
    }

    @GetMapping("/manager")
    public String manager(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if (!"MANAGER".equals(role) && !"ADMIN".equals(role)) return "redirect:/login";
        return "forward:/manager.html";
    }

    @GetMapping("/employee")
    public String employee(HttpSession session) {
        if (session.getAttribute("userRole") == null) return "redirect:/login";
        return "forward:/employee.html";
    }
}
