//GoogleContactsService.java
package com.pili.oauth2login.service;

import com.pili.oauth2login.model.PeopleResponse;
import com.pili.oauth2login.model.Person;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class GoogleContactsService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleContactsService.class);
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final WebClient webClient;

    @Autowired
    public GoogleContactsService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
        this.webClient = WebClient.builder().build();
    }

    public PeopleResponse getContacts(OAuth2AuthenticationToken authentication) {
        try {
            OAuth2AuthorizedClient client = getAuthorizedClient(authentication);
            String accessToken = client.getAccessToken().getTokenValue();
            logger.debug("Access token obtained for contacts retrieval");
            
            // Google People API endpoint for contacts
            // Update this line in the getContacts method:
            String url = "https://people.googleapis.com/v1/people/me/connections" +
            "?personFields=names,emailAddresses,photos,phoneNumbers" + // Add phoneNumbers here
            "&pageSize=100";
            
            return webClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                        response -> {
                            logger.error("Error calling Google API: {}", response.statusCode());
                            return Mono.error(new RuntimeException("Error calling Google API: " + response.statusCode()));
                        })
                    .bodyToMono(PeopleResponse.class)
                    .block();
                    
        } catch (WebClientResponseException e) {
            logger.error("WebClient error in getContacts: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error retrieving contacts: " + e.getStatusText(), e);
        } catch (Exception e) {
            logger.error("Error in getContacts", e);
            throw new RuntimeException("Error retrieving contacts", e);
        }
    }
    
    public Person getUserDetails(OAuth2AuthenticationToken authentication) {
        try {
            OAuth2AuthorizedClient client = getAuthorizedClient(authentication);
            String accessToken = client.getAccessToken().getTokenValue();
            logger.debug("Access token obtained for user details retrieval");
            
            // Google People API endpoint for user profile
            String url = "https://people.googleapis.com/v1/people/me" +
                    "?personFields=names,emailAddresses,addresses,birthdays,phoneNumbers,genders,ageRanges,photos";
            
            return webClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                    response -> {
                        logger.error("Error calling Google API: {}", response.statusCode());
                        return Mono.error(new RuntimeException("Error calling Google API: " + response.statusCode()));
                    })
                    .bodyToMono(Person.class)
                    .block();
                    
        } catch (WebClientResponseException e) {
            logger.error("WebClient error in getUserDetails: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error retrieving user details: " + e.getStatusText(), e);
        } catch (Exception e) {
            logger.error("Error in getUserDetails", e);
            throw new RuntimeException("Error retrieving user details", e);
        }
    }
    
    private OAuth2AuthorizedClient getAuthorizedClient(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName());
                
        if (client == null) {
            logger.error("OAuth2 client is null - authorization required");
            throw new RuntimeException("Not authorized. Please authenticate with Google first.");
        }
        
        return client;
    }
}