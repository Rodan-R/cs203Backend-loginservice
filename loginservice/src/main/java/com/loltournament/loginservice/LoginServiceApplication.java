package com.loltournament.loginservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableJpaRepositories("com.loltournament.loginservice.repository")  // Make sure this is scanning the correct package
@EntityScan("com.loltournament.loginservice.model")  // Ensure JPA knows where to find your entities
public class LoginServiceApplication {

	public static void main(String[] args) {
		// Load environment variables from .env
        Dotenv dotenv = Dotenv.load();
        System.setProperty("SPRING_DATASOURCE_URL", dotenv.get("SPRING_DATASOURCE_URL"));
        System.setProperty("SPRING_DATASOURCE_USERNAME", dotenv.get("SPRING_DATASOURCE_USERNAME"));
        System.setProperty("SPRING_DATASOURCE_PASSWORD", dotenv.get("SPRING_DATASOURCE_PASSWORD"));
        SpringApplication.run(LoginServiceApplication.class, args);
	}

}
