package dk.dtu.roborally.config;

import dk.dtu.roborally.engine.GameEngine;
import dk.dtu.roborally.engine.publishers.GameEventPublisher;
import dk.dtu.roborally.engine.services.GameService;
import dk.dtu.roborally.engine.services.LobbyService;
import dk.dtu.roborally.engine.services.UserService;
import dk.dtu.roborally.engine.services.SessionService;
import dk.dtu.roborally.repository.GameRepository;
import dk.dtu.roborally.repository.LobbyRepository;
import dk.dtu.roborally.repository.UserRepository;
import dk.dtu.roborally.repository.sql.SqlGameRepository;
import dk.dtu.roborally.repository.sql.SqlLobbyRepository;
import dk.dtu.roborally.repository.sql.SqlUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class EngineConfig {

	@Bean
	public UserRepository userRepository(DataSource dataSource) {
		return new SqlUserRepository(dataSource);
	}

	@Bean
	public LobbyRepository lobbyRepository(DataSource dataSource,
			UserRepository userRepository) {
		return new SqlLobbyRepository(dataSource, userRepository);
	}

	@Bean
	public GameRepository gameRepository(DataSource dataSource,
			LobbyRepository lobbyRepository) {
		return new SqlGameRepository(dataSource, lobbyRepository);
	}

	@Bean
	public GameEngine gameEngine(GameRepository gameRepository,
			UserRepository userRepository, LobbyRepository lobbyRepository,
			GameEventPublisher gameEventPublisher) {
		return new GameEngine(gameRepository, userRepository, lobbyRepository,
				gameEventPublisher);
	}

	@Bean
	public GameService gameService(GameEngine gameEngine) {
		return gameEngine.getGameService();
	}

	@Bean
	public LobbyService lobbyService(GameEngine gameEngine) {
		return gameEngine.getLobbyService();
	}

	@Bean
	public UserService userService(GameEngine gameEngine) {
		return gameEngine.getUserService();
	}

	@Bean
	public SessionService sessionService(UserRepository userRepository) {
		return new SessionService(userRepository);
	}
}
