package com.loltournament.loginservice.controller;

import com.loltournament.loginservice.model.Player;
import com.loltournament.loginservice.security.SecurityConfig;
import com.loltournament.loginservice.util.JwtUtil;
import com.loltournament.loginservice.service.PlayerService;
// import com.loltournament.loginservice.exception.UserNotFoundException;
import com.loltournament.loginservice.exception.InvalidCredentialsException;

import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    // Initialize logger
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PlayerService userService;

    /**
     * Register a new user with LOCAL authentication provider.
     * Player submits their username, password, email, and playername.
     */
    @PostMapping(value = "/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody Player user) {
        try {
            // The 'user' object should contain the 'playername' value from the request body.
            System.out.println("Player Name: " + user.getPlayername());
            if (user.getPlayername() == null || user.getPlayername().isEmpty()) {
                throw new IllegalArgumentException("Playername cannot be null or empty");
            }

            // Password encoding and saving the user
            user.setPassword(new SecurityConfig().passwordEncoder().encode(user.getPassword()));
            userService.saveUser(user);

            // Create a success response with a structured JSON object
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "User registered successfully!");

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            // Create an error response in case of an exception
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error registering user");
            errorResponse.put("error", e.getMessage());

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Player user) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

            UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
            String jwt = jwtUtil.generateToken(userDetails.getUsername());

            // Return a structured JSON response with the JWT token
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("jwt", jwt);

            return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .body(responseBody);
        } catch (BadCredentialsException ex) {
            logger.error("Invalid credentials for user: {}", user.getUsername(), ex);
            throw new InvalidCredentialsException("Invalid username or password");
        } catch (Exception ex) {
            logger.error("An error occurred during login: {}", ex.getMessage(), ex);
            throw new RuntimeException("An error occurred during login", ex);
        }
    }
}
