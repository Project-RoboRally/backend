package dk.dtu.roborally.models;

import dk.dtu.roborally.loaders.JsonLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameLobbyTest {
	private User owner;
	private GameLobby lobby;

	@BeforeEach
	void setUp() {
		owner = new User("August");
		lobby = new GameLobby(JsonLoader.loadConfig(), "Test lobby", owner);
	}

	@Test
	void creatingLobbyAddsOwnerAsMember() {
		assertSame(owner, lobby.getLobbyOwner());
		assertEquals(1, lobby.getUsersInLobby().size());
		assertTrue(lobby.getUsersInLobby().contains(owner));
	}

	@Test
	void joiningTwiceDoesNotDuplicateMembership() {
		int lobbyCount = lobby.getUsersInLobby().size();
		User user = new User("Alice");

		lobby.addUserToLobby(user);
		lobby.addUserToLobby(user);

		assertEquals(lobbyCount + 1, lobby.getUsersInLobby().size());
		assertTrue(lobby.getUsersInLobby().contains(user));
	}

	@Test
	void seventhUserIsRejectedWithoutChangingMembership() {
		fillUpLobby();
		User extraUser = new User("Extra");

		assertTrue(lobby.isFull());

		assertThrows(IllegalStateException.class,
				() -> lobby.addUserToLobby(extraUser));

		assertEquals(6, lobby.getUsersInLobby().size());
		assertFalse(lobby.getUsersInLobby().contains(extraUser));
	}

	@Test
	void existingMemberCanRejoinFullLobby() {
		fillUpLobby();

		assertDoesNotThrow(() -> lobby.addUserToLobby(owner));

		assertEquals(6, lobby.getUsersInLobby().size());
		assertTrue(lobby.getUsersInLobby().contains(owner));
	}

	@Test
	void sixthMemberIsAcceptedAndFillsLobby() {
		// Owner plus four users makes five members.
		for (int i = 1; i <= 4; i++) {
			lobby.addUserToLobby(new User("User" + i));
		}

		assertFalse(lobby.isFull());

		User sixthUser = new User("Sixth");
		assertDoesNotThrow(() -> lobby.addUserToLobby(sixthUser));

		assertTrue(lobby.isFull());
		assertEquals(6, lobby.getUsersInLobby().size());
		assertTrue(lobby.getUsersInLobby().contains(sixthUser));
		assertTrue(lobby.hasEnoughPlayers());
	}

	@Test
	void secondMemberSatisfiesMinimumPlayerCount() {
		assertFalse(lobby.hasEnoughPlayers());

		lobby.addUserToLobby(new User("Alice"));

		assertTrue(lobby.hasEnoughPlayers());
	}

	private void fillUpLobby() {
		// The owner already occupies the first place.
		for (int i = 1; i <= 5; i++) {
			lobby.addUserToLobby(new User("User" + i));
		}
	}
}
