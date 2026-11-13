package dk.dtu.roborally.api;

import dk.dtu.roborally.api.controllers.GameController;
import dk.dtu.roborally.engine.services.GameService;
import dk.dtu.roborally.engine.services.SessionService;
import dk.dtu.roborally.exceptions.game.GameNotFoundException;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.repository.inmemory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests that game actions take the user from the session token and map errors
 * to the right status codes.
 *
 * @author Elias
 */
@WebMvcTest(GameController.class)
@Import(GameControllerTest.Config.class)
class GameControllerTest {

	@TestConfiguration
	static class Config {
		@Bean
		InMemoryUserRepository userRepository() {
			return new InMemoryUserRepository();
		}

		@Bean
		SessionService sessionService(InMemoryUserRepository userRepository) {
			return new SessionService(userRepository);
		}

		@Bean
		GameService gameService() {
			return mock(GameService.class);
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private InMemoryUserRepository userRepository;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private GameService gameService;

	private String aliceToken;

	@BeforeEach
	void setUp() {
		reset(gameService);
		User alice = userRepository.getByUsername("alice").orElseGet(() -> {
			User user = new User("alice");
			userRepository.add(user);
			return user;
		});
		aliceToken = sessionService.createSession(alice);
	}

	@Test
	void anActionWithoutATokenGives401() throws Exception {
		mockMvc.perform(post("/api/games/game1/upgrade/skip"))
				.andExpect(status().isUnauthorized());

		verifyNoInteractions(gameService);
	}

	@Test
	void theUserComesFromTheTokenNotFromTheBody() throws Exception {
		mockMvc.perform(post("/api/games/game1/choice")
				.header("Authorization", "Bearer " + aliceToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"bob\",\"choice\":\"LEFT\"}"))
				.andExpect(status().isNoContent());

		verify(gameService).makeChoice("game1", "alice", "LEFT");
	}

	@Test
	void anUnknownGameGives404() throws Exception {
		doThrow(new GameNotFoundException("missing")).when(gameService)
				.skipUpgrade("missing", "alice");

		mockMvc.perform(post("/api/games/missing/upgrade/skip")
				.header("Authorization", "Bearer " + aliceToken))
				.andExpect(status().isNotFound());
	}
}
