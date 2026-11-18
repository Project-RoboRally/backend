package dk.dtu.roborally.loaders;

import dk.dtu.roborally.models.board.Board;

/**
 * A board a lobby can be played on. It comes from a level file under resources
 * (path), or from a file the lobby owner uploaded (json).
 *
 * @param id
 *            its key in config.json, which the frontend sends when picking; for
 *            an uploaded board, see {@link #uploaded}
 * @param name
 *            shown to players, from the level's _metadata.name
 * @param path
 *            level file under resources, as passed to {@link LevelLoader}; null
 *            for an uploaded board
 * @param json
 *            the uploaded file, kept so the lobby can be saved and restored
 *            (lobbies.level_json); null for a board from resources
 *
 * @author August
 */
public record BoardInfo(String id, String name, String path, String json) {

	/** A board from a level file under resources. */
	public BoardInfo(String id, String name, String path) {
		this(id, name, path, null);
	}

	/**
	 * A board a lobby owner uploaded. Its id is worked out from the file's
	 * text, so a new file gets a new id (and every player's preview reloads),
	 * and a restored lobby gets the same id back without storing it.
	 */
	public static BoardInfo uploaded(String json) {
		String name = JsonLoader.parseLevel(json).nameOr("Custom board");
		return new BoardInfo("custom-" + Integer.toHexString(json.hashCode()),
				name, null, json);
	}

	/** Builds this board's spaces and elements. */
	public Board load() {
		return json != null
				? LevelLoader.fromJson(json)
				: LevelLoader.load(path);
	}
}
