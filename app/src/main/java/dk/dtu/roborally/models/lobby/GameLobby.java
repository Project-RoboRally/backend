package dk.dtu.roborally.models.lobby;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.loaders.Config;
import dk.dtu.roborally.loaders.LevelLoader;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.game.Game;

/**
 * The Server needs to hold n lobbies, and every lobby can hold a singular game.
 *
 * @author Victor, Sebastian, August
 */

public class GameLobby {
	private final Map<String, LobbyMember> lobbyUsers = new HashMap<>();
	private static final int MIN_USERS = 2;
	private static final int MAX_USERS = 6;

	@Getter
	@Setter
	private Game game;
	private String levelPath;
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

	public void removeUser(String userId) {
		Objects.requireNonNull(userId);
		lobbyUsers.remove(userId);
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

	public void startGame(Game game) {

		if (!hasEnoughPlayers()) {
			throw new IllegalStateException(
					"Not enough players to start the game.");
		}

		game.start();
	}

	public Set<User> getUsersInLobby() {
		return lobbyUsers.values().stream().map(LobbyMember::user)
				.collect(Collectors.toUnmodifiableSet());
	}

	public Map<String, PlayerColor> getColorAssignments() {
		return lobbyUsers.entrySet().stream()
				.collect(Collectors.toUnmodifiableMap(Map.Entry::getKey,
						entry -> entry.getValue().color()));
	}

	public boolean isFull() {
		return lobbyUsers.size() >= MAX_USERS;
	}

	public boolean hasEnoughPlayers() {
		return lobbyUsers.size() >= MIN_USERS;
	}
}
