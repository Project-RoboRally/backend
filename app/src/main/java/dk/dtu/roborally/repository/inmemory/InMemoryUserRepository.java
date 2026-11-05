package dk.dtu.roborally.repository.inmemory;

import com.google.gson.Gson;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.repository.UserRepository;

import java.util.ArrayList;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * In-memory user repository with optional file-backed account persistence.
 *
 * @author Elias, August, Anton
 */
public class InMemoryUserRepository implements UserRepository {
	private final List<User> users = new ArrayList<>();
	private final Gson gson = new Gson();
	private final Path storeFile;

	public InMemoryUserRepository() {
		this(null);
	}

	public InMemoryUserRepository(Path storeFile) {
		this.storeFile = storeFile;
		loadUsers();
	}

	/**
	 * Adds a user to the list of all users
	 *
	 * @param user
	 *            Instance of user
	 * @return true if added, false if username already exists
	 */
	@Override
	public synchronized boolean add(User user) {
		if (existsByUsername(user.getUsername()) || existsById(user.getId()))
			return false;
		users.add(user);
		try {
			saveUsers();
		} catch (RuntimeException ex) {
			users.remove(user);
			throw ex;
		}
		return true;
	}

	@Override
	public synchronized boolean existsByUsername(String username) {
		return users.stream().anyMatch(u -> u.getUsername().equals(username));
	}

	@Override
	public synchronized boolean existsById(String id) {
		return users.stream().anyMatch(u -> u.getId().equals(id));
	}

	@Override
	public synchronized Optional<User> getById(String id) {
		return users.stream().filter(p -> p.getId().equals(id)).findFirst();
	}

	@Override
	public synchronized Optional<User> getByUsername(String username) {
		return users.stream().filter(u -> u.getUsername().equals(username))
				.findFirst();
	}

	@Override
	public synchronized List<User> getAll() {
		return List.copyOf(users);
	}

	@Override
	public synchronized void delete(String id) {
		List<User> removedUsers = users.stream()
				.filter(user -> user.getId().equals(id)).toList();
		if (!removedUsers.isEmpty()) {
			users.removeAll(removedUsers);
			try {
				saveUsers();
			} catch (RuntimeException ex) {
				users.addAll(removedUsers);
				throw ex;
			}
		}
	}

	private void loadUsers() {
		if (storeFile == null || !Files.exists(storeFile)) {
			return;
		}
		try (var reader = Files.newBufferedReader(storeFile)) {
			StoredUser[] storedUsers = gson.fromJson(reader,
					StoredUser[].class);
			if (storedUsers != null) {
				for (StoredUser storedUser : storedUsers) {
					users.add(new User(storedUser.username(),
							storedUser.passwordHash()));
				}
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(
					"Could not read account store " + storeFile, ex);
		}
	}

	private void saveUsers() {
		if (storeFile == null) {
			return;
		}

		Path parent = storeFile.toAbsolutePath().getParent();
		Path temporaryFile = storeFile
				.resolveSibling(storeFile.getFileName() + ".tmp");
		List<StoredUser> storedUsers = users.stream()
				.map(user -> new StoredUser(user.getUsername(),
						user.getPasswordHash()))
				.toList();
		try {
			Files.createDirectories(parent);
			Files.writeString(temporaryFile, gson.toJson(storedUsers));
			try {
				Files.move(temporaryFile, storeFile,
						StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException ex) {
				Files.move(temporaryFile, storeFile,
						StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(
					"Could not save account store " + storeFile, ex);
		}
	}

	private record StoredUser(String username, String passwordHash) {
	}
}
