package dk.dtu.roborally.api;

import dk.dtu.roborally.api.controllers.LobbyController;
import dk.dtu.roborally.engine.services.LobbyService;
import dk.dtu.roborally.engine.services.SessionService;
import dk.dtu.roborally.loaders.BoardCatalog;
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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/**
 * @author Nicoleta
 */

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
		when(lobbyService.getLobby("lobby1")).thenReturn(new GameLobby(
				BoardCatalog.load().defaultBoard(), "Lobby", alice));
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

	@Test
	void changingTheBoardIsDoneAsTheTokensUser() throws Exception {
		GameLobby lobby = lobbyService.getLobby("lobby1");
		when(lobbyService.changeBoard("lobby1", "alice", "test_board"))
				.thenReturn(lobby);

		mockMvc.perform(post("/api/lobbies/lobby1/board")
				.header("Authorization", "Bearer " + aliceToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"boardId\":\"test_board\"}"))
				.andExpect(status().isOk());

		verify(lobbyService).changeBoard("lobby1", "alice", "test_board");
	}

	@Test
	void anUploadedBoardReachesTheServiceAsTheFilesText() throws Exception {
		String file = "{\"squares\": [{\"x1\": 0, \"y1\": 0, \"x2\": 3, \"y2\": 3}]}";
		GameLobby lobby = lobbyService.getLobby("lobby1");
		when(lobbyService.uploadBoard("lobby1", "alice", file))
				.thenReturn(lobby);

		mockMvc.perform(post("/api/lobbies/lobby1/board/custom")
				.header("Authorization", "Bearer " + aliceToken)
				.contentType(MediaType.APPLICATION_JSON).content(file))
				.andExpect(status().isOk());

		verify(lobbyService).uploadBoard("lobby1", "alice", file);
	}

	@Test
	void uploadingWithoutATokenGives401() throws Exception {
		mockMvc.perform(post("/api/lobbies/lobby1/board/custom")
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isUnauthorized());

		verify(lobbyService, never()).uploadBoard(any(), any(), any());
	}

	@Test
	void theLobbysBoardLayoutHasSpacesButNoRobots() throws Exception {
		mockMvc.perform(get("/api/lobbies/lobby1/board"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.width").value(greaterThan(0)))
				.andExpect(jsonPath("$.spaces.length()").value(greaterThan(0)))
				.andExpect(jsonPath("$.robots").value(empty()));
	}

	@Test
	void changingUserAmountAllowedReadsTheNumberFromTheBody() throws Exception {
		GameLobby lobby = lobbyService.getLobby("lobby1");
		lobby.changeUserAmountAllowed(4);
		when(lobbyService.changeUserAmount("lobby1", "alice", 4))
				.thenReturn(lobby);

		// The same JSON names the frontend sends and reads
		mockMvc.perform(post("/api/lobbies/lobby1/amount")
				.header("Authorization", "Bearer " + aliceToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"userAmountAllowed\":4}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userAmountAllowed").value(4));

		verify(lobbyService).changeUserAmount("lobby1", "alice", 4);
	}
}
