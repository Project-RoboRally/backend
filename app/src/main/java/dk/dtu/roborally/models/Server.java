package dk.dtu.roborally.models;

import dk.dtu.roborally.loaders.Config;
import dk.dtu.roborally.loaders.JsonLoader;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Server object that holds all info.
 *
 * @author Victor, Sebastian
 */

public class Server {
	@Getter
	@Setter
	private List<GameLobby> lobbies = new ArrayList<>();
	private static Config config;

	@Getter
	private final Set<User> usersSignedIn = new HashSet<>();

	public Server() {
		// We make the server here, and allows people to sign into it
		config = JsonLoader.loadConfig();
		addLobby(new GameLobby(config));
	}

	public void addLobby(GameLobby lobby) {
		lobbies.add(lobby);
	}

	public void addPlayerToServer(User user) {
		usersSignedIn.add(user);
	}

}
