package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.exceptions.auth.LoginException;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.repository.UserRepository;
import dk.dtu.roborally.repository.inmemory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
	private UserService userService;

	@BeforeEach
	void setUp() {
		userService = new UserService(new InMemoryUserRepository(),
				new BCryptPasswordEncoder());
	}

	@Test
	void registerStoresAHashNotThePlainPassword() {
		User user = userService.addUser("alice", "password123");

		assertNotEquals("password123", user.getPasswordHash());
		assertTrue(new BCryptPasswordEncoder().matches("password123",
				user.getPasswordHash()));
	}

	@Test
	void loginWorksWithTheRightPassword() {
		userService.addUser("alice", "password123");

		User user = userService.authenticate("alice", "password123");

		assertEquals("alice", user.getUsername());
	}

	@Test
	void loginFailsWithTheWrongPassword() {
		userService.addUser("alice", "password123");

		assertThrows(LoginException.class,
				() -> userService.authenticate("alice", "wrong-password"));
	}

	@Test
	void loginFailsForAnUnknownUser() {
		assertThrows(LoginException.class,
				() -> userService.authenticate("nobody", "password123"));
	}

	@Test
	void cannotRegisterTheSameUsernameTwice() {
		userService.addUser("alice", "password123");

		assertThrows(IllegalStateException.class,
				() -> userService.addUser("alice", "otherpass123"));
	}

	@Test
	void shortPasswordsAreRejected() {
		assertThrows(IllegalArgumentException.class,
				() -> userService.addUser("alice", "short"));
	}

	@Test
	void userWithoutPasswordCannotLogIn() {
		UserRepository repository = new InMemoryUserRepository();
		repository.add(new User("ghost"));
		UserService service = new UserService(repository,
				new BCryptPasswordEncoder());

		assertThrows(LoginException.class,
				() -> service.authenticate("ghost", "anything123"));
	}

	@Test
	void tooLongPasswordsAreRejected() {
		String longPassword = "a".repeat(73);

		assertThrows(IllegalArgumentException.class,
				() -> userService.addUser("alice", longPassword));
	}
}
