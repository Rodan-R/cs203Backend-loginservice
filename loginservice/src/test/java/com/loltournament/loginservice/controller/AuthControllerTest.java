package com.loltournament.loginservice.controller;

import com.loltournament.loginservice.model.Player;
import com.loltournament.loginservice.service.PlayerService;
import com.loltournament.loginservice.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Should add more tests
 * 
 * Missing tests:
 * EmailAlreadyExists
 * UsernameAlreadyExists
 * PlayernameAlreadyExists
 * NullUsername
 * NullPassword
 * NullEmail
 * NullPlayername
 * 
 * And others that you can think of
 */
@SpringBootTest
@AutoConfigureMockMvc // To enable MockMvc for testing
public class AuthControllerTest {

    @Mock
    private PlayerService playerService; // Mocking PlayerService

    @Mock
    private AuthenticationManager authenticationManager; // Mocking AuthenticationManager

    @Mock
    private JwtUtil jwtUtil; // Mocking JwtUtil for JWT token generation

    @InjectMocks
    private AuthController authController; // Inject the mocks into AuthController

    @Autowired
    private MockMvc mockMvc; // MockMvc to perform HTTP requests

    @Autowired
    private ObjectMapper objectMapper; // ObjectMapper for JSON processing

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this); // Initialize the mocks
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build(); // Setup MockMvc with the controller
    }

    // Test case for successful registration
    @Test
    public void testRegisterUserSuccess() throws Exception {
        Player player = new Player();
        player.setUsername("testuser");
        player.setPassword("testpassword");
        player.setEmail("test@gmail.com");
        player.setPlayername("testplayer");

        // Mock PlayerService saveUser method
        doNothing().when(playerService).saveUser(any(Player.class));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(player)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        // Verify that the saveUser method is called once
        verify(playerService, times(1)).saveUser(any(Player.class));
    }

    // Test case for failed registration
    @Test
    public void testRegisterUserFailure() throws Exception {
        Player player = new Player();
        player.setUsername("testuser");
        player.setPassword("testpassword");
        player.setPlayername(null); // Ensure playername is null to trigger the failure

        // Simulate an exception being thrown during user registration
        doThrow(new IllegalArgumentException("Playername cannot be null or empty"))
                .when(playerService).saveUser(any(Player.class));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(player)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Error registering user"));

        // Verify that saveUser was not called because of the validation error
        verify(playerService, times(0)).saveUser(any(Player.class));
    }


    // Test case for successful login
    @Test
    public void testLoginUserSuccess() throws Exception {
        Player player = new Player();
        player.setUsername("testuser");
        player.setPassword("testpassword");

        // Mock the successful authentication and UserDetails retrieval
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // Successful authentication
        UserDetails mockUserDetails = mock(UserDetails.class); // Mocking UserDetails
        when(mockUserDetails.getUsername()).thenReturn("testuser");
        when(playerService.loadUserByUsername(anyString())).thenReturn(mockUserDetails); // Mock UserDetails from service
        when(jwtUtil.generateToken(anyString())).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(player)))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer fake-jwt-token"))
                .andExpect(jsonPath("$.jwt").value("fake-jwt-token"));

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(playerService, times(1)).loadUserByUsername(anyString());
        verify(jwtUtil, times(1)).generateToken(anyString());
    }

    @Test
    public void testLoginUserFailure() throws Exception {
        Player player = new Player();
        player.setUsername("testuser");
        player.setPassword("wrongpassword");

        // Simulate a failed authentication by throwing a BadCredentialsException
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(player)))
                .andExpect(status().isUnauthorized()) // Expecting 401 Unauthorized
                .andExpect(jsonPath("$.message").value("Invalid username or password"));

        // Verify that the authentication was attempted
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
