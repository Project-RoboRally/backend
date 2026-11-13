package dk.dtu.roborally.api;

import dk.dtu.roborally.api.controllers.LobbyController;
import dk.dtu.roborally.engine.services.LobbyService;
import dk.dtu.roborally.engine.services.SessionService;
import dk.dtu.roborally.loaders.JsonLoader;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.lobby.GameLobby;
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

@WebMvcTest(LobbyController.class)
@Import(LobbyControllerTest.Config.class)
class LobbyControllerTest {

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
		LobbyService lobbyService() {
			return mock(LobbyService.class);
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private InMemoryUserRepository userRepository;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private LobbyService lobbyService;

	private String aliceToken;

	@BeforeEach
	void setUp() {
		reset(lobbyService);
		User alice = userRepository.getByUsername("alice").orElseGet(() -> {
			User user = new User("alice");
			userRepository.add(user);
			return user;
		});
		aliceToken = sessionService.createSession(alice);
		// the kick endpoint returns the lobby afterwards
		when(lobbyService.getLobby("lobby1")).thenReturn(
				new GameLobby(JsonLoader.loadConfig(), "Lobby", alice));
	}

	@Test
	void anActionWithoutATokenGives401() throws Exception {
		mockMvc.perform(post("/api/lobbies/lobby1/leave"))
				.andExpect(status().isUnauthorized());

		verifyNoInteractions(lobbyService);
	}

	@Test
	void kickingIsDoneAsTheTokensUser() throws Exception {
		mockMvc.perform(post("/api/lobbies/lobby1/kick")
				.header("Authorization", "Bearer " + aliceToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"userKicked\":\"bob\"}"))
				.andExpect(status().isOk());

		verify(lobbyService).kickUser("lobby1", "alice", "bob");
	}
}
