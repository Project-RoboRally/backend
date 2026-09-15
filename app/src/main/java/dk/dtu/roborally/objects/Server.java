package dk.dtu.roborally.objects;

import dk.dtu.roborally.enums.GameState;
import dk.dtu.roborally.handlers.Config;
import dk.dtu.roborally.handlers.JsonLoader;
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
    private List<Lobby> lobbies = new ArrayList<>();
    private JsonLoader jsonLoader;
    private static Config config;

    @Getter
    private final Set<Player> playersSignedIn = new HashSet<>();

    public Server() {
        // We make the server here, and allows people to sign into it
        jsonLoader = new JsonLoader();
        config = JsonLoader.loadConfig();
        System.out.println(config.levels.level_0);
    }

    public void addLobby(Lobby lobby) {
        lobbies.add(lobby);
    }

    public void addPlayerToServer(Player player) {
        playersSignedIn.add(player);
    }

}
