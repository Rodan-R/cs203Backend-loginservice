package com.loltournament.loginservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import com.loltournament.loginservice.model.Player;

public interface PlayerRepository extends JpaRepository<Player, Long>  {
    Optional<Player> findByUsername(String username);
    Optional<Player> findByEmail(String email);
}
