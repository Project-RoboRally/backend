package dk.dtu.roborally.config;

import dk.dtu.roborally.engine.GameEngine;
import dk.dtu.roborally.engine.services.GameService;
import dk.dtu.roborally.engine.services.LobbyService;
import dk.dtu.roborally.engine.services.UserService;
import dk.dtu.roborally.repository.GameRepository;
import dk.dtu.roborally.repository.LobbyRepository;
import dk.dtu.roborally.repository.UserRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryGameRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryLobbyRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EngineConfig {

	@Bean
	public GameRepository gameRepository() {
		return new InMemoryGameRepository();
	}

	@Bean
	public UserRepository userRepository() {
		return new InMemoryUserRepository();
	}

	@Bean
	public LobbyRepository lobbyRepository() {
		return new InMemoryLobbyRepository();
	}

	@Bean
	public GameEngine gameEngine(GameRepository gameRepository,
			UserRepository userRepository, LobbyRepository lobbyRepository) {
		return new GameEngine(gameRepository, userRepository, lobbyRepository);
	}

	@Bean
	public LobbyService lobbyService(GameEngine gameEngine) {
		return gameEngine.getLobbyService();
	}

	@Bean
	public GameService gameService(GameEngine gameEngine) {
		return gameEngine.getGameService();
	}

	@Bean
	public UserService userService(GameEngine gameEngine) {
		return gameEngine.getUserService();
	}
}
