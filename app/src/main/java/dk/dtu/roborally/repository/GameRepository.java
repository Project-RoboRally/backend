package dk.dtu.roborally.repository;

import dk.dtu.roborally.models.Game;

import java.util.List;
import java.util.Optional;

/**
 * Basic interface for games repository
 *
 * @author Elias
 */
public interface GameRepository {
    boolean add(Game game);
    boolean exists(String gameID);
    Optional<Game> getById(String gameID);
    List<Game> getAll();
    void delete(String gameID);
}
