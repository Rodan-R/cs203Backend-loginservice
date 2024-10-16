
package com.loltournament.loginservice.controller;

import com.loltournament.loginservice.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.http.HttpStatus;

/**
 * For the tests to work, change the register credentials each time 
 * (i know this is super scuffed, i made this at 4am on the day of presentation)
 * 
 * To fix the above issue:
 * Should mock the database, then use AfterEach to delete the row from the mocked database
 * Right now this is using the actual database !!!
 * 
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private String baseUrl;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port + "/auth";
    }

    @Test
    public void testRegisterUser_Success() throws URISyntaxException {
        Player player = new Player();
        player.setUsername("testuser8");
        player.setPassword("testpassword8");
        player.setEmail("test8@gmail.com");
        player.setPlayername("test8player");

        HttpEntity<Player> request = new HttpEntity<>(player);
        ResponseEntity<String> response = restTemplate.postForEntity(new URI(baseUrl + "/register"), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("User registered successfully!");
    }

    @Test
    public void testLoginUser_Success() throws URISyntaxException {
        // Create the Player object (simulating login)
        Player player = new Player();
        player.setUsername("testuser8");
        player.setPassword("testpassword8");

        // Create HttpEntity object
        HttpEntity<Player> request = new HttpEntity<>(player);

        // Perform HTTP request
        ResponseEntity<String> response = restTemplate.postForEntity(new URI(baseUrl + "/login"), request, String.class);

        // Validate the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get(HttpHeaders.AUTHORIZATION)).isNotNull();
        assertThat(response.getBody()).contains("jwt");
    }
}
