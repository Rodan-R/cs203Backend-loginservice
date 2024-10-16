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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc // If you're using MockMvc for testing the controller
public class AuthControllerTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this); 
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    public void testRegisterUserSuccess() throws Exception {
        // Mock player data
        Player player = new Player();
        player.setUsername("testuser");
        player.setPassword("testpassword");
        player.setEmail("test@gmail.com");
        player.setPlayername("testplayer");

        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("message", "User registered successfully!");

        // Mock saveUser call
        doNothing().when(playerService).saveUser(any(Player.class));

        // Perform POST request
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(player)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        // Verify interactions
        verify(playerService, times(1)).saveUser(any(Player.class));
    }

    @Test
    public void testRegisterUserFailure() throws Exception {
        Player player = new Player();
        player.setUsername("testuser");
        player.setPassword("testpassword");

        doThrow(new IllegalArgumentException("Playername cannot be null or empty"))
                .when(playerService).saveUser(any(Player.class));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(player)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Error registering user"));
    }

    @Test
    public void testLoginUserSuccess() throws Exception {
        Player player = new Player();
        player.setUsername("testuser");
        player.setPassword("testpassword");

        // Mock JWT generation and authentication
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // AuthenticationManager doesn't return anything
        when(jwtUtil.generateToken(anyString())).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(player)))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer fake-jwt-token"))
                .andExpect(jsonPath("$.jwt").value("fake-jwt-token"));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(anyString());
    }

    @Test
    public void testLoginUserFailure() throws Exception {
        Player player = new Player();
        player.setUsername("testuser");
        player.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Invalid username or password"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(player)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An error occurred during login"));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
