package com.loltournament.loginservice.controller;

import com.loltournament.loginservice.model.Player;
import com.loltournament.loginservice.security.SecurityConfig;
import com.loltournament.loginservice.util.JwtUtil;
import com.loltournament.loginservice.service.PlayerService;
import com.loltournament.loginservice.exception.UserNotFoundException;
import com.loltournament.loginservice.exception.InvalidCredentialsException;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;

import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
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

    @PostMapping(value = "/register", produces = "application/json") // Explicitly setting content-type as JSON
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody Player user) {
        try {
            // Password encoding and saving the user
            user.setPassword(new SecurityConfig().passwordEncoder().encode(user.getPassword()));
            userService.saveUser(user);
            
            // AWS Lambda Invocation logic
            AWSLambda lambdaClient = AWSLambdaClientBuilder.defaultClient();
            String jsonPayload = String.format("{\"playerName\":\"%s\", \"email\":\"%s\"}", user.getUsername(), user.getEmail());

            InvokeRequest invokeRequest = new InvokeRequest()
                    .withFunctionName("RegisterConfirmationLambda")
                    .withPayload(jsonPayload);
            
            InvokeResult result = lambdaClient.invoke(invokeRequest);
            String response = new String(result.getPayload().array(), StandardCharsets.UTF_8);
            System.out.println("Lambda response: " + response);

            // Create a success response with a structured JSON object
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "User registered successfully!");
            responseBody.put("lambdaResponse", response);

            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            // Create an error response in case of an exception
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error invoking Lambda function");
            errorResponse.put("error", e.getMessage());

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/login", produces = "application/json") // Ensure JSON content-type
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
                // Log invalid credentials
                logger.error("Invalid credentials for user: {}", user.getUsername(), ex);
                throw new InvalidCredentialsException("Invalid username or password");
            } catch (Exception ex) {
                // Log any unexpected error
                logger.error("An error occurred during login: {}", ex.getMessage(), ex);
                throw new RuntimeException("An error occurred during login");
            }
    }

    @GetMapping(value = "/oauth2/success", produces = "application/json") // Explicitly setting content-type as JSON
    public ResponseEntity<Map<String, Object>> oauth2LoginSuccess(@AuthenticationPrincipal OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        Player user = userService.findOrCreateUserByEmail(email, Player.AuthProvider.GOOGLE);

        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        String jwt = jwtUtil.generateToken(user.getUsername());
        
        // Return a structured JSON response with the JWT token
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("jwt", jwt);
        
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}
