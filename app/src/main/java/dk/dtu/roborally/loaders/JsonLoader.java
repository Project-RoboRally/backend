package dk.dtu.roborally.loaders;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Loads the json files in resources, like the config and the levels.
 *
 * @author Victor, Sebastian
 */

public class JsonLoader {
	private static final Gson GSON = new Gson();

	public static Config loadConfig() {
		return load("config.json", Config.class);
	}

	public static LevelData loadLevel(String levelPath) {
		return load(levelPath, LevelData.class);
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
