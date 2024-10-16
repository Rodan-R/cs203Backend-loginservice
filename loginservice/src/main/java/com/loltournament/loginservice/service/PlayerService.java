package com.loltournament.loginservice.service;

import com.loltournament.loginservice.model.Player;
import com.loltournament.loginservice.repository.PlayerRepository;

import jakarta.transaction.Transactional;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PlayerService implements UserDetailsService {

    @Autowired
    private PlayerRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public void saveUser(Player user) {
        // user.setAuthProvider(Player.AuthProvider.LOCAL);
        userRepository.save(user);
    }

    // Find or create a user for Google OAuth2 login
    public Player findOrCreateUserByEmail(String email) {
        Optional<Player> optionalUser = userRepository.findByEmail(email);
        
        // If the user is not found, create a new one
        if (optionalUser.isEmpty()) {
            Player newUser = new Player();
            newUser.setEmail(email);
            // newUser.setAuthProvider(provider);  // Set the auth provider (e.g., GOOGLE)
            // Set other necessary details such as default username or password
            userRepository.save(newUser);
            return newUser;
        }
    
        Player user = optionalUser.get();  // Retrieve the existing user
        // Update the user's authProvider if it's not set to GOOGLE (in case they registered locally)
        // if (!user.getAuthProvider().equals(provider)) {
        //     user.setAuthProvider(provider);
        //     userRepository.save(user);
        // }
    
        return user;  // Return the existing or updated user
    }
}
