package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.models.Player;
import dk.dtu.roborally.repository.PlayerRepository;


/**
 * Service for all player logic
 *
 * @author Elias
 */
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }


    public void addPlayer(String username) {
        if (username == null)
            throw new IllegalArgumentException("Username is null");

        if (username.isBlank())
            throw new IllegalArgumentException("Username may not be empty");

        if (username.contains(" "))
            throw new IllegalArgumentException("Username may not contain spaces");

        if (username.length() < 3 || username.length() > 16)
            throw new IllegalArgumentException("Username should be between 3 and 16 characters");

        if (playerRepository.exists(username))
            throw new IllegalStateException("Player already exists");

        Player player = new Player(username);
        playerRepository.add(player);
    }
}
