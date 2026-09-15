package dk.dtu.roborally.repository;

import dk.dtu.roborally.models.Player;

import java.util.List;
import java.util.Optional;

/**
 * Basic interface for player repository
 *
 * @author Elias
 */
public interface PlayerRepository {
    boolean add(Player player);

    boolean exists(String username);

    Optional<Player> getById(String username);

    List<Player> getAll();

    void delete(String username);
}
