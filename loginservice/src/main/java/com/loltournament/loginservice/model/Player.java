package com.loltournament.loginservice.model;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
// import jakarta.persistence.EnumType;
// import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * I commented out the authProvider
 */ 
@Entity
@Table(name = "players")
public class Player implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "playername")
    private String playername;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    // @Enumerated(EnumType.STRING)
    // private AuthProvider authProvider;

    // public enum AuthProvider {
    //     LOCAL, GOOGLE
    // }

    public Long getId() {
        return userId;
    }
    public void setId(Long userId) {
        this.userId = userId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPlayername() {
		return playername;
	}
	public void setPlayername(String playername) {
		this.playername = playername;
	}
    

    // public AuthProvider getAuthProvider() {
    //     return authProvider;
    // }

    // public void setAuthProvider(AuthProvider authProvider) {
    //     this.authProvider = authProvider;
    // }

    // Methods required by UserDetails

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;  // Implement authorities if needed
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
