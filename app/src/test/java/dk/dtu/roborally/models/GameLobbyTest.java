package dk.dtu.roborally.models;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.loaders.JsonLoader;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.models.lobby.LobbyMember;
import dk.dtu.roborally.models.player.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

	@Test
	void ownerReceivesAColor() {
		assertNotNull(lobby.getColorAssignments().get(owner.getId()));
	}

	@Test
	void everyMemberReceivesADistinctColor() {
		fillUpLobby();

		var assignments = lobby.getColorAssignments();

		assertEquals(6, assignments.size());
		assertEquals(6, new HashSet<>(assignments.values()).size());

		for (User member : lobby.getUsersInLobby()) {
			assertTrue(assignments.containsKey(member.getId()));
		}
	}

	@Test
	void rejoiningPreservesColor() {
		PlayerColor original = lobby.getColorAssignments().get(owner.getId());

		lobby.addUserToLobby(owner);

		assertEquals(original, lobby.getColorAssignments().get(owner.getId()));
	}

	@Test
	void leavingReleasesColor() {
		fillUpLobby();

		User departing = lobby.getUsersInLobby().stream()
				.filter(user -> !user.getId().equals(owner.getId())).findFirst()
				.orElseThrow();

		PlayerColor released = lobby.getColorAssignments()
				.get(departing.getId());

		lobby.removeUser(departing.getId());

		assertFalse(lobby.getColorAssignments().containsKey(departing.getId()));

		User replacement = new User("Replacement");
		lobby.addUserToLobby(replacement);

		assertEquals(released,
				lobby.getColorAssignments().get(replacement.getId()));
	}

	@Test
	void membersStartOutNotReadyAndCanToggleReady() {
		User alice = new User("Alice");
		lobby.addUserToLobby(alice);

		assertFalse(readyOf(alice));

		lobby.setReady(alice.getId(), true);
		assertTrue(readyOf(alice));

		lobby.setReady(alice.getId(), false);
		assertFalse(readyOf(alice));
	}

	@Test
	void settingReadyForNonMemberThrows() {
		assertThrows(IllegalArgumentException.class,
				() -> lobby.setReady("not-a-member", true));
	}

	@Test
	void memberCanSwitchToAFreeColorAndReleasesTheOldOne() {
		PlayerColor original = lobby.getColorAssignments().get(owner.getId());

		lobby.changeColor(owner.getId(), PlayerColor.GREEN);

		assertEquals(PlayerColor.GREEN,
				lobby.getColorAssignments().get(owner.getId()));

		// The released color is free again for the next member.
		User alice = new User("Alice");
		lobby.addUserToLobby(alice);
		assertEquals(original, lobby.getColorAssignments().get(alice.getId()));
	}

	@Test
	void cannotTakeAColorAnotherMemberHas() {
		User alice = new User("Alice");
		lobby.addUserToLobby(alice);
		PlayerColor alicesColor = lobby.getColorAssignments()
				.get(alice.getId());
		PlayerColor ownersColor = lobby.getColorAssignments()
				.get(owner.getId());

		assertThrows(IllegalStateException.class,
				() -> lobby.changeColor(owner.getId(), alicesColor));
		assertEquals(ownersColor,
				lobby.getColorAssignments().get(owner.getId()));
	}

	@Test
	void choosingYourOwnColorAgainIsAllowed() {
		PlayerColor own = lobby.getColorAssignments().get(owner.getId());

		assertDoesNotThrow(() -> lobby.changeColor(owner.getId(), own));
	}

	@Test
	void cannotChangeColorAfterTheGameStarted() {
		User alice = new User("Alice");
		lobby.addUserToLobby(alice);
		lobby.setReady(alice.getId(), true);
		lobby.startGame();

		assertThrows(IllegalStateException.class,
				() -> lobby.changeColor(owner.getId(), PlayerColor.GREEN));
	}

	@Test
	void changingColorForNonMemberThrows() {
		assertThrows(IllegalArgumentException.class,
				() -> lobby.changeColor("not-a-member", PlayerColor.GREEN));
	}

	@Test
	void startRequiresEveryMemberExceptOwnerToBeReady() {
		User alice = new User("Alice");
		lobby.addUserToLobby(alice);

		// Alice is not ready; the owner never needs a ready flag.
		assertThrows(IllegalStateException.class, () -> lobby.startGame());

		lobby.setReady(alice.getId(), true);
		assertDoesNotThrow(() -> lobby.startGame());
	}

	@Test
	void startWithTooFewPlayersThrows() {
		assertThrows(IllegalStateException.class, () -> lobby.startGame());
	}

	@Test
	void startingTwiceThrows() {
		User alice = new User("Alice");
		lobby.addUserToLobby(alice);
		lobby.setReady(alice.getId(), true);

		lobby.startGame();

		assertThrows(IllegalStateException.class, () -> lobby.startGame());
	}

	@Test
	void startGameGivesEveryMemberADistinctRobotOnAStartingPoint() {
		fillUpLobby();
		for (LobbyMember member : lobby.getMembers()) {
			lobby.setReady(member.user().getId(), true);
		}

		Game game = lobby.startGame();

		// One player per lobby member.
		assertEquals(6, game.getPlayersInGame().size());

		// Every player keeps the color they were assigned in the lobby.
		Map<String, PlayerColor> colors = lobby.getColorAssignments();
		for (Player player : game.getPlayersInGame()) {
			assertEquals(colors.get(player.getUser().getId()),
					player.getColor());
		}

		// Robots occupy distinct positions, each one a starting point, and
		// face that starting point's orientation.
		Map<Vector2D, Direction> startingPoints = lobby.getLevel()
				.getStartingPoints().stream()
				.collect(Collectors.toMap(point -> point.getPosition(),
						point -> point.getOrientation()));

		Set<Vector2D> occupied = game.getPlayersInGame().stream()
				.map(player -> player.getRobot().getCurrentSpace())
				.collect(Collectors.toSet());
		assertEquals(6, occupied.size());

		for (Player player : game.getPlayersInGame()) {
			Vector2D position = player.getRobot().getCurrentSpace();
			assertTrue(startingPoints.containsKey(position));
			assertEquals(startingPoints.get(position),
					player.getRobot().getOrientation());
		}

		// The lobby remembers which game it started.
		assertEquals(game.getGameID(), lobby.getGameId());
		assertSame(lobby.getLevel(), game.getBoard());
	}

	private boolean readyOf(User user) {
		return lobby.getMembers().stream()
				.filter(member -> member.user().getId().equals(user.getId()))
				.findFirst().orElseThrow().ready();
	}

	private void fillUpLobby() {
		// The owner already occupies the first place.
		for (int i = 1; i <= 5; i++) {
			lobby.addUserToLobby(new User("User" + i));
		}
	}
}
