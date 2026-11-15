package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.engine.publishers.PresencePublisher;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.exceptions.game.GameNotFoundException;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.PlayerPresence;
import dk.dtu.roborally.models.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of which players in a game are still connected. Clients send a
 * health ping every {@link #TICK_MILLIS} ms and {@link #tick()} runs just as
 * often. A player who misses {@link #MISSES_BEFORE_OFFLINE} ticks in a row is
 * offline until their next ping.
 *
 * @author Elias
 */
public class PresenceService {

	public static final long TICK_MILLIS = 10_000;
	public static final int MISSES_BEFORE_OFFLINE = 2;

	private final GameService gameService;
	private final PresencePublisher publisher;
	/** Game id to username to health, for games someone has pinged in. */
	private final Map<String, Map<String, Health>> games = new HashMap<>();

	public PresenceService(GameService gameService,
			PresencePublisher publisher) {
		this.gameService = gameService;
		this.publisher = publisher;
	}

	/** Records a health ping. Ignored if the user is not playing the game. */
	public synchronized void recordPing(String gameId, String username) {
		Game game = gameService.getGame(gameId);
		if (game.getGameStatus() != GameStatus.IN_PROGRESS
				|| findPlayer(game, username) == null) {
			return;
		}
		Health health = healthOf(game).get(username);
		health.pinged = true;
		health.missed = 0;
		if (!health.online) {
			health.online = true;
			publisher.publishPresence(gameId, presence(game));
		}
	}

	/**
	 * Counts a miss for everyone who did not ping since the last tick and
	 * sends the presence of every tracked game.
	 */
	public synchronized void tick() {
		for (String gameId : List.copyOf(games.keySet())) {
			Game game = findGame(gameId);
			if (game == null || game.getGameStatus() != GameStatus.IN_PROGRESS) {
				games.remove(gameId);
				continue;
			}
			for (Health health : healthOf(game).values()) {
				if (health.pinged) {
					health.pinged = false;
				} else if (++health.missed >= MISSES_BEFORE_OFFLINE) {
					health.online = false;
				}
			}
			publisher.publishPresence(gameId, presence(game));
		}
	}

	/** Everyone in the game, sorted by username. */
	public synchronized List<PlayerPresence> presence(String gameId) {
		return presence(gameService.getGame(gameId));
	}

	private List<PlayerPresence> presence(Game game) {
		Map<String, Health> healths = healthOf(game);
		List<PlayerPresence> result = new ArrayList<>();
		for (Player player : game.getPlayersInGame()) {
			String username = player.getUser().getUsername();
			boolean online = healths.get(username).online;
			result.add(new PlayerPresence(username, online,
					!online && game.isWaitingOn(player)));
		}
		result.sort(Comparator.comparing(PlayerPresence::username));
		return result;
	}

	private Map<String, Health> healthOf(Game game) {
		Map<String, Health> healths = games.computeIfAbsent(game.getGameID(),
				id -> new HashMap<>());
		for (Player player : game.getPlayersInGame()) {
			healths.computeIfAbsent(player.getUser().getUsername(),
					username -> new Health());
		}
		return healths;
	}

	private Game findGame(String gameId) {
		try {
			return gameService.getGame(gameId);
		} catch (GameNotFoundException e) {
			return null;
		}
	}

	private static Player findPlayer(Game game, String username) {
		for (Player player : game.getPlayersInGame()) {
			if (player.getUser().getUsername().equals(username)) {
				return player;
			}
		}
		return null;
	}

	private static class Health {
		private boolean pinged;
		private int missed;
		private boolean online = true;
	}
}
