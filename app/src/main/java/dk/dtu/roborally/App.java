package dk.dtu.roborally;

import dk.dtu.roborally.engine.GameEngine;
import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Main class for the application. Here we intialize the game engine and start
 * the Spring API.
 *
 * @author Elias & Nicoleta
 */

public class App {

	@Getter
	private final GameEngine gameEngine;

	public App(String[] args) {
		ConfigurableApplicationContext context = SpringApplication
				.run(BackendApplication.class, args);
		this.gameEngine = context.getBean(GameEngine.class);
	}
}
