package dk.dtu.roborally.models.lobby;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.loaders.BoardInfo;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.elements.Checkpoint;
import dk.dtu.roborally.models.board.elements.PriorityAntenna;
import dk.dtu.roborally.models.board.elements.StartingPoint;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;

/**
 * The Server needs to hold n lobbies, and every lobby can initiate a game, if
 * there is the right amount of players and they are ready.
 *
 * @author Victor, Sebastian, August, Elias
 */

public class GameLobby {
	private final Map<String, LobbyMember> lobbyUsers = new HashMap<>(); // string
																			// =
																			// userId
	private final Map<String, User> kickedUsers = new LinkedHashMap<>(); // userId,
																			// user
																			// in
																			// kick
																			// order
	private static final int MIN_USERS = 2;
	private static final int MAX_USERS = 6;
	@Getter
	private int userAmountAllowed = 6;

	@Getter
	private String gameId;

	/** Which board was picked; {@link #level} is that board loaded. */
	@Getter
	private BoardInfo board;
	@Getter
	private Board level;

	@Getter
	private final String lobbyId;
	@Getter
	private String lobbyName;
	@Getter
	private User lobbyOwner;

	public GameLobby(BoardInfo board, String lobbyName, User lobbyOwner) {
		this(java.util.UUID.randomUUID().toString(), board, lobbyName,
				lobbyOwner);
		addUserToLobby(lobbyOwner);
	}

	/**
	 * Rebuilds a stored lobby. Membership and the linked game are applied with
	 * {@link #restoreMembers}.
	 */
	public GameLobby(String lobbyId, BoardInfo board, String lobbyName,
			User lobbyOwner) {
		this.lobbyId = Objects.requireNonNull(lobbyId);
		this.lobbyName = Objects.requireNonNull(lobbyName);
		this.lobbyOwner = Objects.requireNonNull(lobbyOwner);
		this.board = Objects.requireNonNull(board);
		this.level = board.load();
	}

	/**
	 * Where {@link #board} is stored: its level file (lobbies.level_path), or
	 * null when it was uploaded.
	 */
	public String getLevelPath() {
		return board.path();
	}

	/**
	 * The uploaded file when {@link #board} was uploaded (lobbies.level_json),
	 * otherwise null.
	 */
	public String getLevelJson() {
		return board.json();
	}

	/**
	 * Switches the lobby to another board, from resources or uploaded. The game
	 * is built from {@link #level} when it starts, so the game is played on
	 * this board.
	 */
	public void changeBoard(BoardInfo nextBoard) {
		Objects.requireNonNull(nextBoard);

		if (isGameStarted()) {
			throw new IllegalStateException(
					"The game has already been started.");
		}

		if (nextBoard.id().equals(board.id())) {
			return;
		}

		Board nextLevel = nextBoard.load();
		checkPlayable(nextBoard.name(), nextLevel);

		this.board = nextBoard;
		this.level = nextLevel;

		// Everyone readied up for the old board, so they confirm the new one
		lobbyUsers.replaceAll((userId, member) -> member.withReady(false));
	}

	/**
	 * Checks if an uplaoded board can be played
	 */
	private static void checkPlayable(String name, Board candidate) {
		long startPositions = candidate.getStartingPoints().stream()
				.map(point -> point.getPosition()).distinct().count();
		// Players can join after the board is picked, so room for a full lobby
		if (startPositions < MAX_USERS) {
			throw new IllegalArgumentException(name + " has " + startPositions
					+ " different starting points, and needs " + MAX_USERS
					+ ".");
		}

		// Priority throws when a round starts without one
		if (candidate.getElements(PriorityAntenna.class).isEmpty()) {
			throw new IllegalArgumentException(
					name + " has no priority antenna.");
		}

		// A player wins on reaching checkpoint n after 1, 2, ... in order, so
		// a missing number would make the game impossible to win
		List<Integer> numbers = candidate.getElements(Checkpoint.class).stream()
				.map(Checkpoint::getNumber).sorted().toList();
		if (numbers.isEmpty() || !numbers.equals(
				IntStream.rangeClosed(1, numbers.size()).boxed().toList())) {
			throw new IllegalArgumentException(name
					+ " needs checkpoints numbered 1, 2, 3 ... with none missing or repeated.");
		}
	}
	public void changeUserAmountAllowed(int newUserAmountAllowed) {
		if (isGameStarted()) {
			throw new IllegalStateException(
					"The game has already been started.");
		}
		if (newUserAmountAllowed > MAX_USERS
				|| newUserAmountAllowed < MIN_USERS) {
			throw new IllegalArgumentException(
					newUserAmountAllowed + " needs to be 2-6 users");
		}
		if (lobbyUsers.size() > newUserAmountAllowed) {
			throw new IllegalArgumentException(newUserAmountAllowed
					+ " can't be less than the current lobby member amount");
		}
		this.userAmountAllowed = newUserAmountAllowed;
		lobbyUsers.replaceAll((userId, member) -> member.withReady(false));

	}

	/** Replaces membership and the linked game id from stored rows. */
	public void restoreMembers(String gameId, List<LobbyMember> members) {
		this.gameId = gameId;
		lobbyUsers.clear();
		for (LobbyMember member : members) {
			lobbyUsers.put(member.user().getId(), member);
		}
	}

	public void setLobbyName(String lobbyName) {
		this.lobbyName = Objects.requireNonNull(lobbyName);
	}

	public void addUserToLobby(User user) {
		Objects.requireNonNull(user);
		String userId = user.getId();

		if (lobbyUsers.containsKey(userId)) {
			return;
		}

		if (isFull()) {
			throw new IllegalStateException(
					"Game lobby is full. Cannot add more users.");
		}

		if (kickedUsers.containsKey(userId)) {
			throw new IllegalStateException(
					"This user is kicked from this lobby");
		}

		PlayerColor color = findAvailableColor();
		lobbyUsers.put(userId, new LobbyMember(user, color));
	}

	public boolean isGameStarted() {
		return gameId != null;
	}

	public void validateCanStartGame() {
		if (gameId != null) {
			throw new IllegalStateException(
					"The game has already been started.");
		}

		if (!hasEnoughPlayers()) {
			throw new IllegalStateException(
					"Not enough players to start the game.");
		}

		boolean everyoneElseReady = lobbyUsers.values().stream().filter(
				member -> !member.user().getId().equals(lobbyOwner.getId()))
				.allMatch(member -> member.ready());

		if (!everyoneElseReady) {
			throw new IllegalStateException(
					"All players must be ready before the game can start.");
		}

		List<StartingPoint> startingPoints = level.getStartingPoints();

		long distinctPositions = startingPoints.stream()
				.map(point -> point.getPosition()).distinct().count();

		if (distinctPositions != startingPoints.size()) {
			throw new IllegalStateException(
					"The chosen board has duplicate starting positions.");
		}

		if (startingPoints.size() < lobbyUsers.size()) {
			throw new IllegalStateException(
					"This chosen board does not have enough starting points.");
		}
	}

	public void kickUser(String userId) {
		Objects.requireNonNull(userId);

		if (!lobbyUsers.containsKey(userId)) {
			throw new IllegalStateException("Cannot kick users not in lobby");
		}
		if (userId.equals(lobbyOwner.getId())) {
			throw new IllegalStateException("Cannot kick a lobby owner");
		}
		kickedUsers.put(userId, lobbyUsers.get(userId).user());
	}

	public void unKickUser(String userId) {
		Objects.requireNonNull(userId);

		if (!kickedUsers.containsKey(userId)) {
			throw new IllegalStateException(
					"Cannot unkick users that are not kicked");
		}
		kickedUsers.remove(userId);
	}

	public void removeUser(String userId) {
		Objects.requireNonNull(userId);
		lobbyUsers.remove(userId);
	}

	/** Replaces a LobbyMember record with a new one with toggled ready flag */
	public void setReady(String userId, boolean ready) {
		Objects.requireNonNull(userId);
		LobbyMember member = lobbyUsers.get(userId);

		if (member == null) {
			throw new IllegalArgumentException("User is not in this lobby.");
		}

		lobbyUsers.put(userId, member.withReady(ready));
	}

	public void changeColor(String userId, PlayerColor color) {
		Objects.requireNonNull(userId);
		Objects.requireNonNull(color);
		LobbyMember member = lobbyUsers.get(userId);

		if (member == null) {
			throw new IllegalArgumentException("User is not in this lobby.");
		}

		if (isGameStarted()) {
			throw new IllegalStateException(
					"The game has already been started.");
		}

		if (member.color() == color) {
			return;
		}

		boolean taken = lobbyUsers.values().stream()
				.anyMatch(other -> other.color() == color);

		if (taken) {
			throw new IllegalStateException("That color is already taken.");
		}

		lobbyUsers.put(userId, member.withColor(color));
	}

	private PlayerColor findAvailableColor() {
		for (PlayerColor color : PlayerColor.values()) {
			boolean taken = lobbyUsers.values().stream()
					.anyMatch(member -> member.color() == color);

			if (!taken) {
				return color;
			}
		}

		throw new IllegalStateException("No available colours.");
	}

	/**
	 * Creates the Game this lobby has been configured for: every member becomes
	 * a Player whose Robot is placed on a randomly assigned StartingPoint,
	 * facing that point's orientation.
	 */
	public Game startGame() {
		return startGame(new Random());
	}

	/**
	 * Same as {@link #startGame()}, but with an injectable source of randomness
	 * so tests can make the assignment deterministic.
	 */
	public Game startGame(Random random) {
		validateCanStartGame();

		// Sort by userId before shuffling: a seeded Random only gives a
		// reproducible assignment if the input order is reproducible too.
		List<LobbyMember> members = lobbyUsers.entrySet().stream()
				.sorted(Map.Entry.comparingByKey()) // sort by userId
				.map(entry -> entry.getValue())
				.collect(Collectors.toCollection(ArrayList::new));
		Collections.shuffle(members, random);

		List<StartingPoint> startingPoints = level.getStartingPoints();

		// Adding players to set and robots to starting points.
		Set<Player> players = new HashSet<>();
		for (int i = 0; i < members.size(); i++) {
			LobbyMember member = members.get(i);
			StartingPoint startingPoint = startingPoints.get(i);

			Robot robot = new Robot(startingPoint.getPosition(),
					startingPoint.getOrientation());
			players.add(new Player(UUID.randomUUID().toString(), member.user(),
					member.color(), robot));
		}
		// setup game
		Game game = new Game(UUID.randomUUID().toString(), level, players);
		this.gameId = game.getGameID();

		return game;
	}

	public List<LobbyMember> getMembers() {
		return List.copyOf(lobbyUsers.values());
	}

	public List<User> getKickedUsers() {
		return List.copyOf(kickedUsers.values());
	}

	public Set<User> getUsersInLobby() {
		return lobbyUsers.values().stream().map(member -> member.user())
				.collect(Collectors.toUnmodifiableSet());
	}

	public Map<String, PlayerColor> getColorAssignments() {
		return lobbyUsers.entrySet().stream()
				.collect(Collectors.toUnmodifiableMap(entry -> entry.getKey(),
						entry -> entry.getValue().color()));
	}

	public boolean isFull() {
		return lobbyUsers.size() >= userAmountAllowed;
	}

	public boolean hasEnoughPlayers() {
		return lobbyUsers.size() >= MIN_USERS;
	}
}
