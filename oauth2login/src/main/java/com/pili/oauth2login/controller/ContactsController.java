//ContactsController.java
package com.pili.oauth2login.controller;

import com.pili.oauth2login.model.PeopleResponse;
import com.pili.oauth2login.service.GoogleContactsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Controller
public class ContactsController {

    private static final Logger logger = LoggerFactory.getLogger(ContactsController.class);
    private final GoogleContactsService googleContactsService;

    @Autowired
    public ContactsController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping("/contacts")
    public String getContacts(OAuth2AuthenticationToken authentication, Model model) {
        if (authentication == null) {
            logger.warn("Attempted to access /contacts without authentication");
            return "redirect:/";
        }
        
        try {
            PeopleResponse contactsResponse = googleContactsService.getContacts(authentication);
            model.addAttribute("contacts", contactsResponse);
            model.addAttribute("totalContacts", contactsResponse.getTotalItems());
            logger.info("Successfully retrieved contacts");
            return "contacts";
        } catch (Exception e) {
            logger.error("Error retrieving contacts", e);
            model.addAttribute("error", "Failed to retrieve contacts: " + e.getMessage());
            model.addAttribute("errorDetails", e.toString());
            return "error";
        }
    }
    
    @ExceptionHandler(Exception.class)
    public String handleError(Exception e, Model model) {
        logger.error("Unhandled exception in ContactsController", e);
        model.addAttribute("error", "An unexpected error occurred");
        model.addAttribute("errorDetails", e.toString());
        return "error";
    }
}