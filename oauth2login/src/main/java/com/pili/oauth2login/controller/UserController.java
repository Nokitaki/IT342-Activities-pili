//UserController.java
package com.pili.oauth2login.controller;

import com.pili.oauth2login.model.Person;
import com.pili.oauth2login.service.GoogleContactsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final GoogleContactsService googleContactsService;

    @Autowired
    public UserController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping("/user-info")
    public String userInfo(@AuthenticationPrincipal OAuth2User principal, 
                        OAuth2AuthenticationToken authentication,
                        Model model) {
        if (principal == null) {
            logger.warn("Attempted to access /user-info without authentication");
            return "redirect:/";
        }

        model.addAttribute("user", principal.getAttributes());

        try {
            // Fetch detailed profile from People API
            Person userDetails = googleContactsService.getUserDetails(authentication);
            model.addAttribute("userDetails", userDetails);

            // Ensure age is added separately
            if (userDetails.getAge() != null) {
                model.addAttribute("age", userDetails.getAge().getAge());
            } else {
                model.addAttribute("age", "Unknown");
            }

            logger.info("Successfully retrieved user details");
            return "user-info";
        } catch (Exception e) {
            logger.error("Error retrieving user details", e);
            model.addAttribute("error", "Failed to retrieve user details: " + e.getMessage());
            model.addAttribute("errorDetails", e.toString());
            return "error";
        }
    }

    
    @ExceptionHandler(Exception.class)
    public String handleError(Exception e, Model model) {
        logger.error("Unhandled exception in UserController", e);
        model.addAttribute("error", "An unexpected error occurred");
        model.addAttribute("errorDetails", e.toString());
        return "error";
    }
}