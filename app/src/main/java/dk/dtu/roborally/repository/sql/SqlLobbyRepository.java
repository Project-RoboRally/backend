package dk.dtu.roborally.repository.sql;

import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.loaders.Config;
import dk.dtu.roborally.loaders.JsonLoader;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.models.lobby.LobbyMember;
import dk.dtu.roborally.repository.LobbyRepository;
import dk.dtu.roborally.repository.UserRepository;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Postgres storage for lobbies. Each lobby stays cached so joins, ready flags
 * and the linked game id survive a restart.
 */
public class SqlLobbyRepository implements LobbyRepository {

	private final JdbcTemplate jdbc;
	private final TransactionTemplate transactions;
	private final UserRepository users;
	private final Config config;
	private final Map<String, GameLobby> lobbies = new LinkedHashMap<>();

	public SqlLobbyRepository(DataSource dataSource, UserRepository users) {
		SqlSchema.ensure(dataSource);
		this.jdbc = new JdbcTemplate(dataSource);
		this.transactions = new TransactionTemplate(
				new DataSourceTransactionManager(dataSource));
		this.users = users;
		this.config = JsonLoader.loadConfig();
		loadAll();
	}

	@Override
	public synchronized boolean add(GameLobby lobby) {
		if (exists(lobby.getLobbyId())) {
			return false;
		}
		write(lobby, true);
		lobbies.put(lobby.getLobbyId(), lobby);
		return true;
	}

	@Override
	public synchronized boolean exists(String lobbyId) {
		return lobbies.containsKey(lobbyId);
	}

	@Override
	public synchronized Optional<GameLobby> getById(String lobbyId) {
		return Optional.ofNullable(lobbies.get(lobbyId));
	}

	@Override
	public synchronized List<GameLobby> getAll() {
		return List.copyOf(lobbies.values());
	}

	@Override
	public synchronized void delete(String lobbyId) {
		jdbc.update("DELETE FROM lobbies WHERE id = ?", lobbyId);
		lobbies.remove(lobbyId);
	}

	@Override
	public synchronized void update(GameLobby lobby) {
		if (!exists(lobby.getLobbyId())) {
			throw new IllegalStateException(
					"Lobby is not stored: " + lobby.getLobbyId());
		}
		write(lobby, false);
		lobbies.put(lobby.getLobbyId(), lobby);
	}

	private void write(GameLobby lobby, boolean insert) {
		transactions.executeWithoutResult(status -> {
			if (insert) {
				jdbc.update("""
						INSERT INTO lobbies
						    (id, name, owner_id, level_path, game_id)
						VALUES (?, ?, ?, ?, ?)
						""", lobby.getLobbyId(), lobby.getLobbyName(),
						lobby.getLobbyOwner().getId(), lobby.getLevelPath(),
						lobby.getGameId());
			} else {
				int rows = jdbc.update("""
						UPDATE lobbies
						SET name = ?, owner_id = ?, level_path = ?, game_id = ?
						WHERE id = ?
						""", lobby.getLobbyName(),
						lobby.getLobbyOwner().getId(), lobby.getLevelPath(),
						lobby.getGameId(), lobby.getLobbyId());
				if (rows == 0) {
					throw new IllegalStateException(
							"Lobby is not stored: " + lobby.getLobbyId());
				}
				jdbc.update("DELETE FROM lobby_members WHERE lobby_id = ?",
						lobby.getLobbyId());
			}
			for (LobbyMember member : lobby.getMembers()) {
				jdbc.update("""
						INSERT INTO lobby_members
						    (lobby_id, user_id, color, ready)
						VALUES (?, ?, ?, ?)
						""", lobby.getLobbyId(), member.user().getId(),
						member.color().name(), member.ready());
			}
		});
	}

	private void loadAll() {
		Map<String, GameLobby> loaded = new LinkedHashMap<>();
		Map<String, String> gameIds = new HashMap<>();
		jdbc.query("""
				SELECT id, name, owner_id, level_path, game_id
				FROM lobbies
				""", rs -> {
			String id = rs.getString("id");
			User owner = users.getById(rs.getString("owner_id"))
					.orElseThrow(() -> new IllegalStateException(
							"Lobby owner is missing: " + id));
			loaded.put(id, new GameLobby(id, config, rs.getString("name"),
					owner, rs.getString("level_path")));
			gameIds.put(id, rs.getString("game_id"));
		});

		Map<String, List<LobbyMember>> members = new HashMap<>();
		jdbc.query("""
				SELECT lobby_id, user_id, color, ready
				FROM lobby_members
				""", rs -> {
			String lobbyId = rs.getString("lobby_id");
			User user = users.getById(rs.getString("user_id"))
					.orElseThrow(() -> new IllegalStateException(
							"Lobby member is missing: " + lobbyId));
			members.computeIfAbsent(lobbyId, ignored -> new ArrayList<>())
					.add(new LobbyMember(user,
							PlayerColor.valueOf(rs.getString("color")),
							rs.getBoolean("ready")));
		});

		for (GameLobby lobby : loaded.values()) {
			lobby.restoreMembers(gameIds.get(lobby.getLobbyId()),
					members.getOrDefault(lobby.getLobbyId(), List.of()));
			lobbies.put(lobby.getLobbyId(), lobby);
		}
	}
}
