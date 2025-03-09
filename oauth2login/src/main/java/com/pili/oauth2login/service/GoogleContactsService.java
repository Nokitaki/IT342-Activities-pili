package com.pili.oauth2login.service;

import com.pili.oauth2login.model.PeopleResponse;
import com.pili.oauth2login.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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

    /**
     * Retrieves the user's Google Contacts from the Google People API.
     *
     * @param authentication OAuth2 authentication token
     * @return PeopleResponse containing contacts data
     */
    public PeopleResponse getContacts(OAuth2AuthenticationToken authentication) {
        try {
            OAuth2AuthorizedClient client = getAuthorizedClient(authentication);
            String accessToken = client.getAccessToken().getTokenValue();
            logger.debug("Access token obtained for contacts retrieval");

            // Google People API endpoint for fetching contacts
            String url = "https://people.googleapis.com/v1/people/me/connections" +
                    "?personFields=names,emailAddresses,photos,phoneNumbers" +
                    "&pageSize=100";

            return webClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> {
                                logger.error("Error calling Google API: {}", response.statusCode());
                                return Mono.error(new RuntimeException("Error calling Google API: " + response.statusCode()));
                            }
                    )
                    .bodyToMono(PeopleResponse.class)
                    .block(); // Blocking call since we need synchronous response

        } catch (WebClientResponseException e) {
            logger.error("WebClient error in getContacts: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error retrieving contacts: " + e.getStatusText(), e);
        } catch (Exception e) {
            logger.error("Error in getContacts", e);
            throw new RuntimeException("Error retrieving contacts", e);
        }
    }

    /**
     * Retrieves user profile details from the Google People API.
     *
     * @param authentication OAuth2 authentication token
     * @return Person object containing user details
     */
    public Person getUserDetails(OAuth2AuthenticationToken authentication) {
        try {
            OAuth2AuthorizedClient client = getAuthorizedClient(authentication);
            String accessToken = client.getAccessToken().getTokenValue();
            logger.debug("Access token obtained for user details retrieval");

            // Google People API endpoint for user profile
            String url = "https://people.googleapis.com/v1/people/me" +
                    "?personFields=names,emailAddresses,addresses,birthdays,phoneNumbers,genders,photos";

            return webClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> {
                                logger.error("Error calling Google API: {}", response.statusCode());
                                return Mono.error(new RuntimeException("Error calling Google API: " + response.statusCode()));
                            }
                    )
                    .bodyToMono(Person.class)
                    .block(); // Blocking call since we need synchronous response

        } catch (WebClientResponseException e) {
            logger.error("WebClient error in getUserDetails: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error retrieving user details: " + e.getStatusText(), e);
        } catch (Exception e) {
            logger.error("Error in getUserDetails", e);
            throw new RuntimeException("Error retrieving user details", e);
        }
    }

    /**
     * Retrieves the OAuth2AuthorizedClient for the authenticated user.
     *
     * @param authentication OAuth2 authentication token
     * @return OAuth2AuthorizedClient containing access token
     */
    private OAuth2AuthorizedClient getAuthorizedClient(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        if (client == null) {
            logger.error("OAuth2 client is null - authorization required");
            throw new RuntimeException("Not authorized. Please authenticate with Google first.");
        }

        return client;
    }

    public void updateContact(OAuth2AuthenticationToken authentication, String contactId, String newName) {
        OAuth2AuthorizedClient client = getAuthorizedClient(authentication);
        String accessToken = client.getAccessToken().getTokenValue();
    
        // Step 1: Fetch the existing contact to get its etag
        String getUrl = "https://people.googleapis.com/v1/" + contactId + "?personFields=names,etag";
    
        Person existingContact = webClient.get()
                .uri(getUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Person.class)
                .block(); // Blocking call
    
        if (existingContact == null || existingContact.getEtag() == null) {
            throw new RuntimeException("Failed to retrieve contact etag for update.");
        }
    
        // Step 2: Use the retrieved etag in the update request
        String updateUrl = "https://people.googleapis.com/v1/" + contactId + "?updatePersonFields=names";
    
        String requestBody = "{ \"etag\": \"" + existingContact.getEtag() + "\", " +
                             "\"names\": [{\"givenName\": \"" + newName + "\"}] }";
    
        webClient.patch()
                .uri(updateUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
    

    public void deleteContact(OAuth2AuthenticationToken authentication, String contactId) {
        OAuth2AuthorizedClient client = getAuthorizedClient(authentication);
        String accessToken = client.getAccessToken().getTokenValue();
    
        String url = "https://people.googleapis.com/v1/" + contactId + ":deleteContact";
    
        webClient.delete()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
    
    public void createContact(OAuth2AuthenticationToken authentication, String name, String email, String phone) {
        OAuth2AuthorizedClient client = getAuthorizedClient(authentication);
        String accessToken = client.getAccessToken().getTokenValue();
    
        String url = "https://people.googleapis.com/v1/people:createContact";
    
        String requestBody = "{ \"names\": [{\"givenName\": \"" + name + "\"}], " +
                             "\"emailAddresses\": [{\"value\": \"" + email + "\"}], " +
                             "\"phoneNumbers\": [{\"value\": \"" + phone + "\"}] }";
    
        webClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
    
}
