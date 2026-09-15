package dk.dtu.roborally.handlers;

import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;

/**
 * This object loads into variables, and can be re-used for the different
 * loaders.
 *
 * @author Victor
 */

public class JsonLoader {
    public static Config loadConfig() {
        try {
            // Load variables into a object instance
            Gson gson = new Gson();

            try (InputStream input = JsonLoader.class
                    .getClassLoader()
                    .getResourceAsStream("config.json")) {

                if (input == null) {
                    throw new RuntimeException("config.json not found");
                }

                return gson.fromJson(
                        new InputStreamReader(input),
                        Config.class);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.json", e);
        }
    }

    public static Level loadLevel(String levelPath) {
        try {
            // Load variables into a object instance
            Gson gson = new Gson();

            try (InputStream input = JsonLoader.class
                    .getClassLoader()
                    .getResourceAsStream(levelPath)) {

                if (input == null) {
                    throw new RuntimeException(levelPath + " not found");
                }

                return gson.fromJson(
                        new InputStreamReader(input),
                        Level.class);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load " + levelPath, e);
        }
    }
}
