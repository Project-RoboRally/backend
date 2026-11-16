package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.engine.publishers.PresencePublisher;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.exceptions.game.GameNotFoundException;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.KickVote;
import dk.dtu.roborally.models.game.PlayerPresence;
import dk.dtu.roborally.models.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Keeps track of which players in a game are still connected, and of votes to
 * kick offline players. Clients send a health ping every {@link #TICK_MS} ms
 * and {@link #tick()} runs just as often. After the first miss the player is
 * asked {@code ARE YOU THERE}; a player who then misses
 * {@link #MISSES_BEFORE_OFFLINE} ticks in a row is offline until their next
 * ping.
 *
 * @author Elias
 */
public class PresenceService {

	public static final long TICK_MS = 10_000;
	public static final int MISSES_BEFORE_OFFLINE = 2; // How many pings to be
														// missed before going
														// offline

	private final GameService gameService;
	private final PresencePublisher publisher;
	/** Game id to username to health, for games someone has pinged in. */
	private final Map<String, Map<String, Health>> games = new HashMap<>();
	private final Map<String, Vote> votes = new HashMap<>(); // Votes for
																// kicking a
																// player

	public PresenceService(GameService gameService,
			PresencePublisher publisher) {
		this.gameService = gameService;
		this.publisher = publisher;
	}

	/** Records a health ping. Ignored if the user is not playing the game. */
	public synchronized void recordPing(String gameId, String username) {
		Game game = gameService.getGame(gameId);
		if (game.getGameStatus() != GameStatus.IN_PROGRESS
				|| findActive(game, username) == null) {
			return;
		}
		Health health = healthOf(game).get(username);
		health.pinged = true;
		health.missed = 0;
		boolean cameOnline = !health.online;
		health.online = true;
		if (cameOnline) {
			Vote vote = votes.get(gameId);
			if (vote != null && vote.target.equals(username)) {
				votes.remove(gameId);
			}
			publish(game, List.of());
		}
	}

	/**
	 * Counts a miss for everyone who did not ping since the last tick and sends
	 * the presence of every tracked game. Players on their last miss before
	 * going offline are asked if they are still there.
	 */
	public synchronized void tick() {
		for (String gameId : List.copyOf(games.keySet())) {
			Game game = findGame(gameId);
			if (game == null
					|| game.getGameStatus() != GameStatus.IN_PROGRESS) {
				games.remove(gameId);
				votes.remove(gameId);
				continue;
			}
			List<String> ask = new ArrayList<>();
			for (Map.Entry<String, Health> entry : healthOf(game).entrySet()) {
				Health health = entry.getValue();
				if (health.pinged) {
					health.pinged = false;
					continue;
				}
				health.missed++;
				if (health.online
						&& health.missed == MISSES_BEFORE_OFFLINE - 1) {
					ask.add(entry.getKey());
				} else if (health.missed >= MISSES_BEFORE_OFFLINE) {
					health.online = false;
				}
			}
			ask.sort(String::compareTo);
			if (!tryCompleteVote(game)) {
				publish(game, ask);
			}
		}
	}

	/**
	 * Starts a vote to kick an offline player. The starter's vote is yes. If
	 * they are the only online player, the kick happens at once.
	 */
	public synchronized void startKickVote(String gameId, String voter,
			String target) {
		Game game = requireInProgress(gameId);
		requireOnline(game, voter);
		Player targetPlayer = findActive(game, target);
		if (targetPlayer == null) {
			throw new IllegalArgumentException(
					"Player is not in this game: " + target);
		}
		if (voter.equals(target)) {
			throw new IllegalArgumentException("You cannot kick yourself");
		}
		if (isOnline(game, target)) {
			throw new IllegalStateException(target + " is still online");
		}
		Vote existing = votes.get(gameId);
		if (existing != null && !existing.target.equals(target)) {
			throw new IllegalStateException(
					"A kick vote for " + existing.target + " is already open");
		}
		Vote vote = existing == null ? new Vote(target) : existing;
		vote.yes.add(voter);
		votes.put(gameId, vote);
		if (!tryCompleteVote(game)) {
			publish(game, List.of());
		}
	}

	/** Casts a yes or no on the open kick vote. No cancels it. */
	public synchronized void voteOnKick(String gameId, String voter,
			boolean yes) {
		Game game = requireInProgress(gameId);
		requireOnline(game, voter);
		Vote vote = votes.get(gameId);
		if (vote == null) {
			throw new IllegalStateException("There is no kick vote open");
		}
		if (vote.target.equals(voter)) {
			throw new IllegalArgumentException(
					"The player being voted on cannot vote");
		}
		if (!yes) {
			votes.remove(gameId);
			publish(game, List.of());
			return;
		}
		vote.yes.add(voter);
		if (!tryCompleteVote(game)) {
			publish(game, List.of());
		}
	}

	/** Everyone still in the game, sorted by username. */
	public synchronized List<PlayerPresence> presence(String gameId) {
		return presence(gameService.getGame(gameId));
	}

	public synchronized KickVote vote(String gameId) {
		Game game = gameService.getGame(gameId);
		return voteView(game);
	}

	private boolean tryCompleteVote(Game game) {
		String gameId = game.getGameID();
		Vote vote = votes.get(gameId);
		if (vote == null) {
			return false;
		}
		if (findActive(game, vote.target) == null
				|| isOnline(game, vote.target)) {
			votes.remove(gameId);
			return false;
		}
		Set<String> needed = onlineVoters(game, vote.target);
		if (!vote.yes.containsAll(needed)) {
			return false;
		}
		votes.remove(gameId);
		gameService.kickPlayer(gameId, vote.target);
		Game updated = gameService.getGame(gameId);
		publish(updated, List.of());
		return true;
	}

	private void publish(Game game, List<String> ask) {
		publisher.publishPresence(game.getGameID(), presence(game), ask,
				voteView(game));
	}

	private KickVote voteView(Game game) {
		Vote vote = votes.get(game.getGameID());
		if (vote == null) {
			return null;
		}
		List<String> needed = new ArrayList<>(onlineVoters(game, vote.target));
		needed.sort(String::compareTo);
		List<String> yes = new ArrayList<>(vote.yes);
		yes.sort(String::compareTo);
		return new KickVote(vote.target, needed, yes);
	}

	private Set<String> onlineVoters(Game game, String target) {
		Set<String> needed = new TreeSet<>();
		for (PlayerPresence player : presence(game)) {
			if (player.online() && !player.username().equals(target)) {
				needed.add(player.username());
			}
		}
		return needed;
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

	private Game requireInProgress(String gameId) {
		Game game = gameService.getGame(gameId);
		if (game.getGameStatus() != GameStatus.IN_PROGRESS) {
			throw new IllegalStateException("The game has already finished.");
		}
		return game;
	}

	private void requireOnline(Game game, String username) {
		if (findActive(game, username) == null) {
			throw new IllegalArgumentException(
					"Player is not in this game: " + username);
		}
		if (!isOnline(game, username)) {
			throw new IllegalStateException("Only online players can vote");
		}
	}

	private boolean isOnline(Game game, String username) {
		Health health = healthOf(game).get(username);
		return health != null && health.online;
	}

	private Game findGame(String gameId) {
		try {
			return gameService.getGame(gameId);
		} catch (GameNotFoundException e) {
			return null;
		}
	}

	private static Player findActive(Game game, String username) {
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

	private static class Vote {
		private final String target;
		private final Set<String> yes = new HashSet<>();

		private Vote(String target) {
			this.target = target;
		}
	}
}
