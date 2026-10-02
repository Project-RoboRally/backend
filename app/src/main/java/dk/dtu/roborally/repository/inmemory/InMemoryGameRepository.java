package dk.dtu.roborally.repository.inmemory;

import dk.dtu.roborally.models.Game;
import dk.dtu.roborally.repository.GameRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * InMemory implementation of Game storage
 *
 * @author Elias
 */
public class InMemoryGameRepository implements GameRepository {
	private final List<Game> games = new ArrayList<>();

	/**
	 * Adds a game to the list of all games
	 *
	 * @param game
	 *            Instance of games
	 * @return true if added, false if gameID already exists
	 */
	@Override
	public boolean add(Game game) {
		if (exists(game.getGameID()))
			return false;

		games.add(game);
		return true;
	}

	@Override
	public boolean exists(String gameID) {
		return games.stream().anyMatch(g -> g.getGameID().equals(gameID));
	}

	@Override
	public Optional<Game> getById(String gameID) {
		return games.stream().filter(g -> g.getGameID().equals(gameID))
				.findFirst();
	}

	@Override
	public List<Game> getAll() {
		return List.copyOf(games);
	}

	@Override
	public void delete(String gameID) {
		games.removeIf(g -> g.getGameID().equals(gameID));
	}
}
