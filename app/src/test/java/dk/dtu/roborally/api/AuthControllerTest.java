package dk.dtu.roborally.api;

import dk.dtu.roborally.api.controllers.AuthController;
import dk.dtu.roborally.engine.services.UserService;
import dk.dtu.roborally.repository.inmemory.InMemoryUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerTest {

	@TestConfiguration
	static class Config {
		@Bean
		UserService userService() {
			return new UserService(new InMemoryUserRepository(),
					new BCryptPasswordEncoder());
		}
	}

	@Autowired
	private MockMvc mockMvc;

	private ResultActions login(String username, String password)
			throws Exception {
		return mockMvc.perform(
				post("/api/login").contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"" + username
								+ "\",\"password\":\"" + password + "\"}"));
	}

	@Test
	void loginWithANewUsernameCreatesTheAccount() throws Exception {
		login("alice", "password123").andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("alice"));
	}

	@Test
	void loginAgainWithTheSamePasswordWorks() throws Exception {
		login("alice", "password123");

		login("alice", "password123").andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("alice"));
	}

	@Test
	void loginWithTheWrongPasswordGives401() throws Exception {
		login("alice", "password123");

		login("alice", "wrong-password").andExpect(status().isUnauthorized());
	}

	@Test
	void newAccountWithAShortPasswordGives400() throws Exception {
		login("alice", "short").andExpect(status().isBadRequest());
	}

	@Test
	void loginWithoutABodyGives400() throws Exception {
		mockMvc.perform(post("/api/login")).andExpect(status().isBadRequest());
	}
}
