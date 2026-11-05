package dk.dtu.roborally.api;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.JsonParser;

import dk.dtu.roborally.engine.services.UserService;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.repository.UserRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryUserRepository;

/**
 * Verifies account endpoints, sessions, CSRF, and credentialed CORS behavior.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.user-store=build/auth-test-users.json")
class AuthControllerTest {

	private static final String ORIGIN = "http://localhost:3000";
	private static final String PASSWORD = "test-password";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void clearAccounts() {
		userRepository.getAll()
				.forEach(user -> userRepository.delete(user.getId()));
	}

	@Test
	void csrfTokenCanBeReturnedAndUsedToRegister() throws Exception {
		CsrfCredentials csrf = getCsrfCredentials();
		assertNotEquals("", csrf.token());

		MvcResult result = mockMvc
				.perform(post("/api/register").cookie(csrf.cookie())
						.header("X-XSRF-TOKEN", csrf.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"accountuser\",\"password\":\""
								+ PASSWORD + "\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("accountuser"))
				.andReturn();

		User account = userRepository.getByUsername("accountuser")
				.orElseThrow();
		assertNotEquals(PASSWORD, account.getPasswordHash());
		assertTrue(account.getPasswordHash().startsWith("$argon2id$"));
		String storedAccounts = Files
				.readString(Path.of("build/auth-test-users.json"));
		assertFalse(storedAccounts.contains(PASSWORD));
		assertFalse(storedAccounts.contains(account.getId()));
		assertTrue(storedAccounts.contains("\"username\""));
		assertTrue(storedAccounts.contains("\"passwordHash\""));

		MockHttpSession session = (MockHttpSession) result.getRequest()
				.getSession(false);
		mockMvc.perform(get("/api/session").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("accountuser"));

		MvcResult lobbyResult = mockMvc.perform(post("/api/lobbies")
				.session(session).cookie(csrf.cookie())
				.header("X-XSRF-TOKEN", csrf.token())
				.contentType(MediaType.APPLICATION_JSON).content(
						"{\"name\":\"Account Lobby\",\"username\":\"forged\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.createdBy").value("accountuser"))
				.andReturn();

		MvcResult otherRegistration = mockMvc.perform(post("/api/register")
				.cookie(csrf.cookie()).header("X-XSRF-TOKEN", csrf.token())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"otheraccount\",\"password\":\""
						+ PASSWORD + "\"}"))
				.andExpect(status().isCreated()).andReturn();
		MockHttpSession otherSession = (MockHttpSession) otherRegistration
				.getRequest().getSession(false);
		String lobbyId = JsonParser
				.parseString(lobbyResult.getResponse().getContentAsString())
				.getAsJsonObject().get("id").getAsString();
		mockMvc.perform(post("/api/lobbies/" + lobbyId + "/rename")
				.session(otherSession).cookie(csrf.cookie())
				.header("X-XSRF-TOKEN", csrf.token())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Renamed Lobby\"}"))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/logout").session(session)
				.cookie(csrf.cookie()).header("X-XSRF-TOKEN", csrf.token()))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/session").session(session))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void duplicateAndInvalidRegistrationAreRejected() throws Exception {
		CsrfCredentials csrf = getCsrfCredentials();
		register(csrf, "duplicateuser", PASSWORD)
				.andExpect(status().isCreated());

		register(csrf, "duplicateuser", PASSWORD)
				.andExpect(status().isConflict());
		register(csrf, "ab", PASSWORD).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").exists());
		register(csrf, "validuser", "short").andExpect(status().isBadRequest());
		register(csrf, "validuser", "😀".repeat(19))
				.andExpect(status().isBadRequest());
	}

	@Test
	void loginChecksPasswordAndReturnsSameErrorForUnknownUsers()
			throws Exception {
		CsrfCredentials csrf = getCsrfCredentials();
		register(csrf, "loginuser", PASSWORD).andExpect(status().isCreated());

		mockMvc.perform(post("/api/login").cookie(csrf.cookie())
				.header("X-XSRF-TOKEN", csrf.token())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"loginuser\",\"password\":\""
						+ PASSWORD + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("loginuser"));

		String wrongPassword = mockMvc.perform(post("/api/login")
				.cookie(csrf.cookie()).header("X-XSRF-TOKEN", csrf.token())
				.contentType(MediaType.APPLICATION_JSON).content(
						"{\"username\":\"loginuser\",\"password\":\"wrong-password\"}"))
				.andExpect(status().isUnauthorized()).andReturn().getResponse()
				.getContentAsString();
		String unknownUsername = mockMvc
				.perform(post("/api/login").cookie(csrf.cookie())
						.header("X-XSRF-TOKEN", csrf.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"unknownuser\",\"password\":\""
								+ PASSWORD + "\"}"))
				.andExpect(status().isUnauthorized()).andReturn().getResponse()
				.getContentAsString();
		assertTrue(wrongPassword.equals(unknownUsername));
	}

	@Test
	void corsPreflightAndUnauthorizedSessionIncludeAllowedOrigin()
			throws Exception {
		mockMvc.perform(options("/api/session").header("Origin", ORIGIN)
				.header("Access-Control-Request-Method", "POST")
				.header("Access-Control-Request-Headers",
						"content-type,x-xsrf-token"))
				.andExpect(status().isOk())
				.andExpect(
						header().string("Access-Control-Allow-Origin", ORIGIN))
				.andExpect(header().string("Access-Control-Allow-Credentials",
						"true"));

		mockMvc.perform(options("/api/session")
				.header("Origin", "http://127.0.0.1:3000")
				.header("Access-Control-Request-Method", "POST")
				.header("Access-Control-Request-Headers",
						"content-type,x-xsrf-token"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin",
						"http://127.0.0.1:3000"));

		mockMvc.perform(get("/api/session").header("Origin", ORIGIN))
				.andExpect(status().isUnauthorized())
				.andExpect(
						header().string("Access-Control-Allow-Origin", ORIGIN))
				.andExpect(content().string(""));
	}

	@Test
	void stateChangingRequestsRequireCsrf() throws Exception {
		mockMvc.perform(
				post("/api/register").contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"csrfuser\",\"password\":\""
								+ PASSWORD + "\"}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void persistedAccountCanBeLoadedByANewRepository(@TempDir Path tempDir)
			throws Exception {
		Path accountFile = tempDir.resolve("users.json");
		InMemoryUserRepository firstRun = new InMemoryUserRepository(
				accountFile);
		User account = new UserService(firstRun).register("persistentuser",
				PASSWORD);

		InMemoryUserRepository afterRestart = new InMemoryUserRepository(
				accountFile);
		assertEquals(account.getUsername(), new UserService(afterRestart)
				.authenticate("persistentuser", PASSWORD).getUsername());
	}

	private org.springframework.test.web.servlet.ResultActions register(
			CsrfCredentials csrf, String username, String password)
			throws Exception {
		return mockMvc.perform(post("/api/register").cookie(csrf.cookie())
				.header("X-XSRF-TOKEN", csrf.token())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"" + username + "\",\"password\":\""
						+ password + "\"}"));
	}

	private CsrfCredentials getCsrfCredentials() throws Exception {
		MvcResult result = mockMvc
				.perform(get("/api/csrf").header("Origin", ORIGIN))
				.andExpect(status().isOk())
				.andExpect(cookie().exists("XSRF-TOKEN"))
				.andExpect(
						header().string("Access-Control-Allow-Origin", ORIGIN))
				.andReturn();
		String token = JsonParser
				.parseString(result.getResponse().getContentAsString())
				.getAsJsonObject().get("token").getAsString();
		return new CsrfCredentials(result.getResponse().getCookie("XSRF-TOKEN"),
				token);
	}

	private record CsrfCredentials(jakarta.servlet.http.Cookie cookie,
			String token) {
	}
}
