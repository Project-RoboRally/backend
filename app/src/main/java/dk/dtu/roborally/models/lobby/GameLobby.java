package dk.dtu.roborally.models.lobby;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.loaders.Config;
import dk.dtu.roborally.loaders.LevelLoader;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.elements.StartingPoint;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;

/**
 * The Server needs to hold n lobbies, and every lobby can initiate a game, if
 * there is the right amount of players and they are ready.
 *
 * @author Victor, Sebastian, August
 */

public class GameLobby {
	private final Map<String, LobbyMember> lobbyUsers = new HashMap<>(); // string
																			// =
																			// userId
	private static final int MIN_USERS = 2;
	private static final int MAX_USERS = 6;

	@Getter
	private String gameId;
	private String levelPath;

	@Getter
	private Board level;
	private Config config;

	@Getter
	private final String lobbyId;
	@Getter
	private String lobbyName;
	@Getter
	private User lobbyOwner;

	public GameLobby(Config config, String lobbyName, User lobbyOwner) {
		this.lobbyId = java.util.UUID.randomUUID().toString();
		this.lobbyName = Objects.requireNonNull(lobbyName);
		this.lobbyOwner = Objects.requireNonNull(lobbyOwner);
		// Create the JsonLoader
		this.config = config;
		this.levelPath = config.levels.level_0;
		this.level = LevelLoader.load(levelPath);

		addUserToLobby(lobbyOwner);
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
		Game game = new Game(UUID.randomUUID().toString(), level, players,
				new EnumMap<>(DamageInstruction.class));
		this.gameId = game.getGameID();

		return game;
	}

	public List<LobbyMember> getMembers() {
		return List.copyOf(lobbyUsers.values());
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
		return lobbyUsers.size() >= MAX_USERS;
	}

	public boolean hasEnoughPlayers() {
		return lobbyUsers.size() >= MIN_USERS;
	}
}
