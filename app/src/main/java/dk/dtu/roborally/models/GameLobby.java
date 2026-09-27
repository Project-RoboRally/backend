package dk.dtu.roborally.models;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import dk.dtu.roborally.loaders.Config;
import dk.dtu.roborally.loaders.LevelLoader;

/**
 * The Server needs to hold n lobbies, and every lobby can hold a singular game.
 *
 * @author Victor, Sebastian
 */

public class GameLobby {
	@Getter
	private final Set<User> usersInLobby = new HashSet<>();

	@Getter
	@Setter
	private Game game;
	private String levelPath;
	private Board level;
	private Config config;

	public GameLobby(Config config) {
		// Create the JsonLoader
		this.config = config;
		this.levelPath = config.levels.level_0;
		this.level = LevelLoader.load(levelPath);
	}

	public void addUserToLobby(User user) {
		usersInLobby.add(user);
	}

	public void startGame(Game game) {
		game.start();
	}
}
