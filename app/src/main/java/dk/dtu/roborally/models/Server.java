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
        config = JsonLoader.loadConfig();
		// We make the server here, and allows people to sign in to it
        User owner = new User("Default user");
        GameLobby lobby = new GameLobby(config, "Default lobby", owner);
        addLobby(lobby);
	}

	public void addLobby(GameLobby lobby) {
		lobbies.add(lobby);
	}

	public void addUserToServer(User user) {
		usersSignedIn.add(user);
	}

}
