package dk.dtu.roborally.repository.sql;

import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.repository.GameRepository;
import dk.dtu.roborally.repository.LobbyRepository;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Postgres storage for games. The row is rewritten after every change, and
 * unfinished games are loaded again when the server starts.
 */
public class SqlGameRepository implements GameRepository {

	private final JdbcTemplate jdbc;
	private final LobbyRepository lobbies;
	private final GameStateCodec codec = new GameStateCodec();
	private final Map<String, Game> games = new LinkedHashMap<>();

	public SqlGameRepository(DataSource dataSource, LobbyRepository lobbies) {
		SqlSchema.ensure(dataSource);
		this.jdbc = new JdbcTemplate(dataSource);
		this.lobbies = lobbies;
		loadAll();
	}

	@Override
	public synchronized boolean add(Game game) {
		if (exists(game.getGameID())) {
			return false;
		}
		jdbc.update("""
				INSERT INTO games (id, status, phase, state_json)
				VALUES (?, ?, ?, CAST(? AS jsonb))
				""", game.getGameID(), game.getGameStatus().name(), phase(game),
				codec.toJson(game));
		games.put(game.getGameID(), game);
		return true;
	}

	@Override
	public synchronized boolean exists(String gameID) {
		return games.containsKey(gameID);
	}

	@Override
	public synchronized Optional<Game> getById(String gameID) {
		return Optional.ofNullable(games.get(gameID));
	}

	@Override
	public synchronized List<Game> getAll() {
		return List.copyOf(games.values());
	}

	@Override
	public synchronized void delete(String gameID) {
		jdbc.update("DELETE FROM games WHERE id = ?", gameID);
		games.remove(gameID);
	}

	@Override
	public synchronized void update(Game game) {
		int rows = jdbc.update("""
				UPDATE games
				SET status = ?, phase = ?, state_json = CAST(? AS jsonb)
				WHERE id = ?
				""", game.getGameStatus().name(), phase(game),
				codec.toJson(game), game.getGameID());
		if (rows == 0) {
			throw new IllegalStateException(
					"Game is not stored: " + game.getGameID());
		}
		games.put(game.getGameID(), game);
	}

	private void loadAll() {
		jdbc.query("SELECT id, state_json::text AS state_json FROM games",
				rs -> {
					String gameId = rs.getString("id");
					GameLobby lobby = lobbyFor(gameId);
					if (lobby == null) {
						return;
					}
					games.put(gameId, codec.toGame(rs.getString("state_json"),
							lobby.getLevel(), lobby));
				});
	}

	private GameLobby lobbyFor(String gameId) {
		for (GameLobby lobby : lobbies.getAll()) {
			if (gameId.equals(lobby.getGameId())) {
				return lobby;
			}
		}
		return null;
	}

	private static String phase(Game game) {
		return game.getCurrentRound().getCurrentPhase().name();
	}
}
