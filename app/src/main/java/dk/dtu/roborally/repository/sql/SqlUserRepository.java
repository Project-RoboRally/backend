package dk.dtu.roborally.repository.sql;

import dk.dtu.roborally.models.User;
import dk.dtu.roborally.repository.UserRepository;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Postgres storage for users. Loaded rows stay in memory so the rest of the
 * server keeps using the same user instances.
 */
public class SqlUserRepository implements UserRepository {

	private final JdbcTemplate jdbc;
	private final Map<String, User> byId = new LinkedHashMap<>();
	private final Map<String, User> byUsername = new HashMap<>();

	public SqlUserRepository(DataSource dataSource) {
		SqlSchema.ensure(dataSource);
		this.jdbc = new JdbcTemplate(dataSource);
		loadAll();
	}

	@Override
	public synchronized boolean add(User user) {
		if (existsByUsername(user.getUsername()) || existsById(user.getId())) {
			return false;
		}
		jdbc.update("INSERT INTO users (id, username) VALUES (?, ?)",
				user.getId(), user.getUsername());
		cache(user);
		return true;
	}

	@Override
	public synchronized boolean existsByUsername(String username) {
		return byUsername.containsKey(username);
	}

	@Override
	public synchronized boolean existsById(String id) {
		return byId.containsKey(id);
	}

	@Override
	public synchronized Optional<User> getById(String id) {
		return Optional.ofNullable(byId.get(id));
	}

	@Override
	public synchronized Optional<User> getByUsername(String username) {
		return Optional.ofNullable(byUsername.get(username));
	}

	@Override
	public synchronized List<User> getAll() {
		return List.copyOf(byId.values());
	}

	@Override
	public synchronized void delete(String id) {
		jdbc.update("DELETE FROM users WHERE id = ?", id);
		User removed = byId.remove(id);
		if (removed != null) {
			byUsername.remove(removed.getUsername());
		}
	}

	private void loadAll() {
		jdbc.query("SELECT id, username FROM users", (rs, rowNum) -> {
			cache(new User(rs.getString("id"), rs.getString("username"),
					null));
			return null;
		});
	}

	private void cache(User user) {
		byId.put(user.getId(), user);
		byUsername.put(user.getUsername(), user);
	}
}
