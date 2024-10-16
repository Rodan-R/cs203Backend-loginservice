package com.loltournament.loginservice.service;

import com.loltournament.loginservice.model.Player;
import com.loltournament.loginservice.repository.PlayerRepository;

import jakarta.transaction.Transactional;

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
        userRepository.save(user);
    }
}
