package dk.dtu.roborally.models;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import dk.dtu.roborally.loaders.Config;
import dk.dtu.roborally.loaders.LevelLoader;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.game.Game;

/**
 * The Server needs to hold n lobbies, and every lobby can hold a singular game.
 *
 * @author Victor, Sebastian, August
 */

public class GameLobby {
	private final Set<User> usersInLobby = new HashSet<>();
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

	public void removeUser(String userId) {
		usersInLobby.removeIf(user -> user.getId().equals(userId));
	}

	public void addUserToLobby(User user) {
		Objects.requireNonNull(user);

		boolean alreadyJoined = usersInLobby.stream()
				.anyMatch(existing -> existing.getId().equals(user.getId()));
		if (alreadyJoined) {
			return;
		}

		if (isFull()) {
			throw new IllegalStateException(
					"Game lobby is full. Cannot add more users.");
		}

		usersInLobby.add(user);
	}

	public void startGame(Game game) {

		if (!hasEnoughPlayers()) {
			throw new IllegalStateException(
					"Not enough players to start the game.");
		}

		game.start();
	}

	public Set<User> getUsersInLobby() {
		return Set.copyOf(usersInLobby);
	}

	public boolean isFull() {
		return usersInLobby.size() >= MAX_USERS;
	}

	public boolean hasEnoughPlayers() {
		return usersInLobby.size() >= MIN_USERS;
	}
}
