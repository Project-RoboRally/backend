package dk.dtu.roborally.repository.inmemory;

import dk.dtu.roborally.models.Player;
import dk.dtu.roborally.repository.PlayerRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * InMemory implementation of Player storage
 *
 * @author Elias
 */
public class InMemoryPlayerRepository implements PlayerRepository {
	private final List<Player> players = new ArrayList<>();

	/**
	 * Adds a player to the list of all players
	 *
	 * @param player
	 *            Instance of player
	 * @return true if added, false if username already exists
	 */
	@Override
	public boolean add(Player player) {
		if (exists(player.getUsername()))
			return false;
		players.add(player);
		return true;
	}

	@Override
	public boolean exists(String username) {
		return players.stream().anyMatch(p -> p.getUsername().equals(username));
	}

	@Override
	public Optional<Player> getById(String username) {
		return players.stream().filter(p -> p.getUsername().equals(username))
				.findFirst();
	}

	@Override
	public List<Player> getAll() {
		return List.copyOf(players);
	}

	@Override
	public void delete(String username) {
		players.removeIf(p -> p.getUsername().equals(username));
	}
}
