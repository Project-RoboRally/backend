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
	 * Adds a user to the list of all users
	 *
	 * @param user
	 *            Instance of user
	 * @return true if added, false if username already exists
	 */
	@Override
	public boolean add(User user) {
		if (existsByUsername(user.getUsername()) || existsById(user.getId()))
			return false;
		users.add(user);
		return true;
	}

	@Override
	public boolean existsByUsername(String username) {
		return users.stream().anyMatch(u -> u.getUsername().equals(username));
	}
    @Override
    public boolean existsById(String id) {
        return users.stream().anyMatch(u -> u.getId().equals(id));
    }

	@Override
	public Optional<User> getById(String id) {
		return users.stream().filter(p -> p.getId().equals(id))
				.findFirst();
	}

	@Override
	public List<User> getAll() {
		return List.copyOf(users);
	}

	@Override
	public void delete(String id) {
		users.removeIf(u -> u.getId().equals(id));
	}
}
