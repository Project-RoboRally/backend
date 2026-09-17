package dk.dtu.roborally;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the backend: starts the Spring Boot REST API This in called
 * from App.java but can also be called directly for testing.
 *
 * @author Nicoleta
 */
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}
