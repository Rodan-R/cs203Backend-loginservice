package com.loltournament.loginservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// import io.github.cdimascio.dotenv.Dotenv;

/**
 * When testing locally, use dotenv to set the config variables
 * I will send a .env file
 * 
 * BUT when building and deploying on server, should uncomment everything
 * related to dotenv since I already put the environment variables
 * in the Task definition of the aws server
 * 
 * Should ask jeremy to edit the environment variables to add your
 * OAuth client id and secret key
 */
@SpringBootApplication
@EnableJpaRepositories("com.loltournament.loginservice.repository")
@EntityScan("com.loltournament.loginservice.model")
public class LoginServiceApplication {

	public static void main(String[] args) {
		// Load environment variables from .env
        // Dotenv dotenv = Dotenv.load();
        // System.setProperty("SPRING_DATASOURCE_URL", dotenv.get("SPRING_DATASOURCE_URL"));
        // System.setProperty("SPRING_DATASOURCE_USERNAME", dotenv.get("SPRING_DATASOURCE_USERNAME"));
        // System.setProperty("SPRING_DATASOURCE_PASSWORD", dotenv.get("SPRING_DATASOURCE_PASSWORD"));
        SpringApplication.run(LoginServiceApplication.class, args);
	}

}
