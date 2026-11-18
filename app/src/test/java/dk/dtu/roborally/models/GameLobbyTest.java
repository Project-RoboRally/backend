package dk.dtu.roborally.models;

import dk.dtu.roborally.helpers.TestResources;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.loaders.BoardCatalog;
import dk.dtu.roborally.loaders.BoardInfo;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.models.lobby.LobbyMember;
import dk.dtu.roborally.models.player.Player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class GameLobbyTest {
	private User owner;
	private GameLobby lobby;

	@BeforeEach
	void setUp() {
		owner = new User("August");
		lobby = new GameLobby(BoardCatalog.load().defaultBoard(), "Test lobby",
				owner);
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

	@Test
	void kickedUserCannotRejoin() {
		User alice = new User("Alice");
		lobby.addUserToLobby(alice);

		lobby.kickUser(alice.getId());
		lobby.removeUser(alice.getId());

		assertThrows(IllegalStateException.class,
				() -> lobby.addUserToLobby(alice));
		assertFalse(lobby.getUsersInLobby().contains(alice));
	}

	@Test
	void unkickedUserCanRejoin() {
		User alice = new User("Alice");
		lobby.addUserToLobby(alice);
		lobby.kickUser(alice.getId());
		lobby.removeUser(alice.getId());
		lobby.unKickUser(alice.getId());

		assertDoesNotThrow(() -> lobby.addUserToLobby(alice));
		assertTrue(lobby.getUsersInLobby().contains(alice));
	}

	@Test
	void kickingOneUserDoesNotBlockOthers() {
		User alice = new User("Alice");
		User bob = new User("Bob");
		lobby.addUserToLobby(alice);
		lobby.kickUser(alice.getId());
		lobby.removeUser(alice.getId());

		assertDoesNotThrow(() -> lobby.addUserToLobby(bob));
		assertTrue(lobby.getUsersInLobby().contains(bob));
	}

	@Test
	void ownerCannotBeKicked() {
		String ownerIdCopy = new String(owner.getId());

		assertThrows(IllegalStateException.class,
				() -> lobby.kickUser(ownerIdCopy));
		assertTrue(lobby.getUsersInLobby().contains(owner));
	}

	@Test
	void cannotKickUserWhoIsNotInTheLobby() {
		User outsider = new User("Outsider");

		assertThrows(IllegalStateException.class,
				() -> lobby.kickUser(outsider.getId()));
	}

	@Test
	void unkickingUserWhoIsNotKickedThrows() {
		User alice = new User("Alice");
		lobby.addUserToLobby(alice);

		assertThrows(IllegalStateException.class,
				() -> lobby.unKickUser(alice.getId()));
	}

	@Test
	void changingTheBoardSwapsWhatTheGameIsBuiltFrom() {
		BoardInfo testBoard = BoardCatalog.load().byId("test_board")
				.orElseThrow();
		User alice = new User("Alice");
		lobby.addUserToLobby(alice);

		lobby.changeBoard(testBoard);
		lobby.setReady(alice.getId(), true);
		Game game = lobby.startGame();

		assertEquals(testBoard, lobby.getBoard());
		assertEquals("levels/test_board.json", lobby.getLevelPath());
		assertSame(lobby.getLevel(), game.getBoard());
	}

	@Test
	void changingTheBoardMakesEveryoneReadyUpAgain() {
		User alice = new User("Alice");
		lobby.addUserToLobby(alice);
		lobby.setReady(alice.getId(), true);

		lobby.changeBoard(BoardCatalog.load().byId("test_board").orElseThrow());

		assertFalse(readyOf(alice));
	}

	@Test
	void pickingTheSameBoardAgainKeepsReadyFlags() {
		User alice = new User("Alice");
		lobby.addUserToLobby(alice);
		lobby.setReady(alice.getId(), true);

		lobby.changeBoard(lobby.getBoard());

		assertTrue(readyOf(alice));
	}

	@Test
	void cannotChangeTheBoardAfterTheGameStarted() {
		User alice = new User("Alice");
		lobby.addUserToLobby(alice);
		lobby.setReady(alice.getId(), true);
		lobby.startGame();
		BoardInfo before = lobby.getBoard();

		assertThrows(IllegalStateException.class, () -> lobby.changeBoard(
				BoardCatalog.load().byId("test_board").orElseThrow()));
		assertEquals(before, lobby.getBoard());
	}

	@Test
	void cannotPickABoardWithTooFewStartingPoints() {
		// This test level has no starting points at all
		BoardInfo noRoom = new BoardInfo("empty", "Empty",
				"levels/level_without_optional_objects.json");
		BoardInfo before = lobby.getBoard();

		assertThrows(IllegalArgumentException.class,
				() -> lobby.changeBoard(noRoom));
		assertEquals(before, lobby.getBoard());
	}

	@Test
	void anUploadedBoardIsWhatTheGameIsBuiltFrom() {
		String json = TestResources.read("levels/test_board.json");
		User alice = new User("Alice");
		lobby.addUserToLobby(alice);

		lobby.changeBoard(BoardInfo.uploaded(json));
		lobby.setReady(alice.getId(), true);
		Game game = lobby.startGame();

		assertEquals("Test board", lobby.getBoard().name());
		assertNull(lobby.getLevelPath());
		assertEquals(json, lobby.getLevelJson());
		assertSame(lobby.getLevel(), game.getBoard());
	}

	@Test
	void aRestoredLobbyRebuildsItsUploadedBoard() {
		BoardInfo uploaded = BoardInfo
				.uploaded(TestResources.read("levels/test_board.json"));

		GameLobby restored = new GameLobby("lobby-id", uploaded, "Restored",
				owner);

		assertEquals(uploaded.id(), restored.getBoard().id());
		assertEquals(6, restored.getLevel().getStartingPoints().size());
	}

	@Test
	void cannotPickABoardWithoutAPriorityAntenna() {
		String json = testBoardEdited(level -> level.remove("antenna"));

		assertThrows(IllegalArgumentException.class,
				() -> lobby.changeBoard(BoardInfo.uploaded(json)));
		assertEquals("level_0", lobby.getBoard().id());
	}

	@Test
	void cannotPickABoardWithAGapInTheCheckpoints() {
		// test_board has checkpoints 1 and 2; 1 and 3 can never be won
		String json = testBoardEdited(level -> level
				.getAsJsonArray("checkpoints").forEach(checkpoint -> {
					JsonObject point = checkpoint.getAsJsonObject();
					if (point.get("number").getAsInt() == 2) {
						point.addProperty("number", 3);
					}
				}));

		assertThrows(IllegalArgumentException.class,
				() -> lobby.changeBoard(BoardInfo.uploaded(json)));
		assertEquals("level_0", lobby.getBoard().id());
	}

	/** test_board.json as an uploaded file, with one thing changed. */
	private static String testBoardEdited(Consumer<JsonObject> edit) {
		JsonObject level = JsonParser
				.parseString(TestResources.read("levels/test_board.json"))
				.getAsJsonObject();
		edit.accept(level);
		return level.toString();
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
