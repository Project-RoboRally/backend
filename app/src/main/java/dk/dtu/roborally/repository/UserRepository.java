package dk.dtu.roborally.repository;

import dk.dtu.roborally.models.User;

import java.util.List;
import java.util.Optional;

/**
 * Basic interface for user repository
 *
 * @author Elias, August
 */
public interface UserRepository {
	boolean add(User user);

	boolean existsByUsername(String username);

    boolean existsById(String id);

	Optional<User> getById(String id);

	List<User> getAll();

	void delete(String id);
}
