package dk.dtu.roborally.loaders;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The boards a lobby can be played on, in the order config.json lists them. The
 * first board is the default for new lobbies.
 *
 * @author August
 */
public final class BoardCatalog {

	private final Map<String, BoardInfo> boards = new LinkedHashMap<>();

	public BoardCatalog(List<BoardInfo> entries) {
		if (entries == null || entries.isEmpty()) {
			throw new IllegalStateException("config.json lists no boards.");
		}
		for (BoardInfo board : entries) {
			if (boards.putIfAbsent(board.id(), board) != null) {
				throw new IllegalStateException(
						"Duplicate board id: " + board.id());
			}
		}
	}

	/**
	 * Reads config.json and the name of every level it lists. Each level is
	 * also built once, so a broken level file stops the server at startup
	 * instead of when an owner picks it.
	 */
	public static BoardCatalog load() {
		Map<String, String> levels = JsonLoader.loadConfig().levels;
		if (levels == null) {
			throw new IllegalStateException("config.json lists no boards.");
		}
		List<BoardInfo> entries = new ArrayList<>();
		levels.forEach((id, path) -> {
			LevelLoader.load(path);
			entries.add(new BoardInfo(id, JsonLoader.loadLevel(path).nameOr(id),
					path));
		});
		return new BoardCatalog(entries);
	}

	public List<BoardInfo> all() {
		return List.copyOf(boards.values());
	}

	public BoardInfo defaultBoard() {
		return boards.values().iterator().next();
	}

	public Optional<BoardInfo> byId(String id) {
		return Optional.ofNullable(boards.get(id));
	}

	/**
	 * Finds the board a stored lobby was saved with (lobbies.level_path). A
	 * board that was later removed from config.json still loads from its file,
	 * so the lobby and any game on it survive a restart.
	 */
	public BoardInfo forStoredPath(String path) {
		return boards.values().stream()
				.filter(board -> board.path().equals(path)).findFirst()
				.orElseGet(() -> new BoardInfo(path, path, path));
	}
}
