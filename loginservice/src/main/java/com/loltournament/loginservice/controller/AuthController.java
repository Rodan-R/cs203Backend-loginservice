package com.loltournament.loginservice.controller;

import com.loltournament.loginservice.model.Player;
import com.loltournament.loginservice.security.SecurityConfig;
import com.loltournament.loginservice.util.JwtUtil;
import com.loltournament.loginservice.service.PlayerService;
// import com.loltournament.loginservice.exception.UserNotFoundException;
// import com.loltournament.loginservice.exception.InvalidCredentialsException;

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

/**
 * AuthController handles authentication-related operations such as user registration and login.
 * It uses Spring Security for authentication and JWT for token generation.
 */
@RestController
@RequestMapping("/auth") // Base URL for all endpoints in this controller
public class AuthController {

    // Logger for this class
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PlayerService userService;

    /**
     * Endpoint to register a new user with LOCAL authentication provider.
     * The user submits their username, password, email, and playername.
     * 
     * @param user Player object containing registration information
     * @return ResponseEntity with a message indicating success or failure
     */
    @PostMapping(value = "/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody Player user) {
        try {
            // Encode the password before saving the user
            user.setPassword(new SecurityConfig().passwordEncoder().encode(user.getPassword()));
            userService.saveUser(user); // Save the user to the database

            // Prepare a success response
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "User registered successfully!");

            return new ResponseEntity<>(responseBody, HttpStatus.OK);

            // MISSING:
            // Should add exceptions for if email/username is already taken
        } catch (Exception e) {
            e.printStackTrace();
            
            // Prepare an error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error registering user");
            errorResponse.put("error", e.getMessage());

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint to log in an existing user.
     * 
     * @param user Player object containing login information
     * @return ResponseEntity with the JWT token if login is successful
     */
    @PostMapping(value = "/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Player user) {
        try {
            // Authenticate the user
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

            // Load user details after successful authentication
            UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
            if (userDetails == null) {
                throw new RuntimeException("Invalid username or password");
            }
            // Generate JWT token for the authenticated user
            String jwt = jwtUtil.generateToken(userDetails.getUsername());

            // Prepare a structured JSON response with the JWT token
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("jwt", jwt);

            // Put JWT token in body and also in the Authorization header
            // Can remove from Authorization header if you want to
            return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt) // Set the Authorization header
                .body(responseBody);
        } catch (BadCredentialsException ex) {
            logger.error("Invalid credentials for user: {}", user.getUsername(), ex);

            // Prepare an error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid username or password");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse); // Return 401 Unauthorized
        } catch (Exception ex) {
            logger.error("An error occurred during login: {}", ex.getMessage(), ex);
            throw new RuntimeException("An error occurred during login", ex);
        }
    }
}
