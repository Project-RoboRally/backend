package dk.dtu.roborally.loaders;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Loads the json files in resources, like the config and the levels.
 *
 * @author Victor, Sebastian, August
 */

public class JsonLoader {
	private static final Gson GSON = new Gson();

	public static Config loadConfig() {
		return load("config.json", Config.class);
	}

	public static LevelData loadLevel(String levelPath) {
		return load(levelPath, LevelData.class);
	}

	/**
	 * Reads level JSON that is not a file under resources, like a board a lobby
	 * owner uploaded. Text that is not a level gives an
	 * IllegalArgumentException, so the upload is answered with 400.
	 */
	public static LevelData parseLevel(String json) {
		LevelData level;
		try {
			level = GSON.fromJson(json, LevelData.class);
		} catch (JsonParseException e) {
			throw new IllegalArgumentException(
					"The board file is not valid JSON for a level.", e);
		}
		if (level == null) {
			throw new IllegalArgumentException("The board file is empty.");
		}
		return level;
	}

	private static <T> T load(String path, Class<T> type) {
		try (InputStream input = JsonLoader.class.getClassLoader()
				.getResourceAsStream(path)) {
			if (input == null) {
				throw new UncheckedIOException(
						new FileNotFoundException(path + " not found"));
			}
			return GSON.fromJson(
					new InputStreamReader(input, StandardCharsets.UTF_8), type);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to load " + path, e);
		}
	}
}
