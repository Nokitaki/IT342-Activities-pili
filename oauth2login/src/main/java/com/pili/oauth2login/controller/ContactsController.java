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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PostMapping("/contacts/create")
    public String createContact(@RequestParam String name, 
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phone,
                                OAuth2AuthenticationToken authentication) {
        if (authentication == null) {
            logger.warn("Unauthorized attempt to create a contact");
            return "redirect:/";
        }

        if ((email == null || email.isBlank()) && (phone == null || phone.isBlank())) {
            logger.warn("Attempted to create contact without email or phone");
            return "redirect:/contacts?error=MissingContactInfo";
        }

        googleContactsService.createContact(authentication, name, email, phone);
        return "redirect:/contacts";
    }

    @PostMapping("/contacts/update")
    public String updateContact(@RequestParam String id, 
                                @RequestParam String etag,
                                @RequestParam String name,
                                OAuth2AuthenticationToken authentication) {
        if (authentication == null) {
            logger.warn("Unauthorized attempt to update a contact");
            return "redirect:/";
        }

        if (id == null || id.isBlank() || etag == null || etag.isBlank()) {
            logger.warn("Invalid contact update request with empty ID or etag");
            return "redirect:/contacts?error=InvalidContactID";
        }

        googleContactsService.updateContact(authentication, id, name);
        return "redirect:/contacts";
    }

    @PostMapping("/contacts/delete")
    public String deleteContact(@RequestParam String id, OAuth2AuthenticationToken authentication) {
        if (authentication == null) {
            logger.warn("Unauthorized attempt to delete a contact");
            return "redirect:/";
        }

        if (id == null || id.isBlank()) {
            logger.warn("Invalid contact delete request with empty ID");
            return "redirect:/contacts?error=InvalidContactID";
        }

        googleContactsService.deleteContact(authentication, id);
        return "redirect:/contacts";
    }

    @ExceptionHandler(Exception.class)
    public String handleError(Exception e, Model model) {
        logger.error("Unhandled exception in ContactsController", e);
        model.addAttribute("error", "An unexpected error occurred");
        model.addAttribute("errorDetails", e.toString());
        return "error";
    }
}
