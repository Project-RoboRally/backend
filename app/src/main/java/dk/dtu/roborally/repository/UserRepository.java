package dk.dtu.roborally.repository;

import dk.dtu.roborally.models.User;

import java.util.List;
import java.util.Optional;

/**
 * Basic interface for player repository
 *
 * @author Elias, August
 */
public interface UserRepository {
	boolean add(User username);

	boolean exists(String username);

	Optional<User> getById(String username);

	List<User> getAll();

	void delete(String username);
}
