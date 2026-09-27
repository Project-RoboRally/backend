package dk.dtu.roborally.repository.inmemory;

import dk.dtu.roborally.models.User;
import dk.dtu.roborally.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * InMemory implementation of Player storage
 *
 * @author Elias, August
 */
public class InMemoryUserRepository implements UserRepository {
	private final List<User> users = new ArrayList<>();

	/**
	 * Adds a player to the list of all players
	 *
	 * @param user
	 *            Instance of player
	 * @return true if added, false if username already exists
	 */
	@Override
	public boolean add(User user) {
		if (exists(user.getUsername()))
			return false;
		users.add(user);
		return true;
	}

	@Override
	public boolean exists(String username) {
		return users.stream().anyMatch(u -> u.getUsername().equals(username));
	}

	@Override
	public Optional<User> getById(String username) {
		return users.stream().filter(p -> p.getUsername().equals(username))
				.findFirst();
	}

	@Override
	public List<User> getAll() {
		return List.copyOf(users);
	}

	@Override
	public void delete(String username) {
		users.removeIf(u -> u.getUsername().equals(username));
	}
}
