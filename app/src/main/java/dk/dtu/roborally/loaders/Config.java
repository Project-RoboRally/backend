package dk.dtu.roborally.loaders;

import java.util.Map;

/**
 * This object is used to store the loaded info from the config
 *
 * @author Victor, August
 */
public class Config {
	/**
	 * Every board a lobby can be played on: board id to level file. Gson keeps
	 * the order of config.json, and the first board is the default.
	 */
	public Map<String, String> levels;
}
