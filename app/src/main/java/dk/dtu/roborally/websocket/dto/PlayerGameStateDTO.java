package dk.dtu.roborally.websocket.dto;

import dk.dtu.roborally.enums.ChoiceType;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.enums.Phase;
import dk.dtu.roborally.enums.UpgradeKind;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.cards.UpgradeCard;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.PendingChoice;
import dk.dtu.roborally.models.game.ProgrammingTimer;
import dk.dtu.roborally.models.game.Round;
import dk.dtu.roborally.models.game.RoundResult;
import dk.dtu.roborally.models.player.Player;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * The full game state as seen by one player: the shared board, round and
 * players, plus that player's private mat. {@code type} identifies the event
 * that caused the message.
 *
 * @author Elias, Nicoleta, Matthias, Sebastian
 */
public record PlayerGameStateDTO(String type, String gameId, GameStatus status,
		RoundDTO round, BoardDTO board, List<PlayerDTO> players, String you,
		PlayerMatDTO playerMat, List<UpgradeDTO> upgradeShop,
		String currentBuyer, ChoiceDTO choice, String winnerUsername) {

	public static final String GAME_STARTED = "GAME_STARTED";
	public static final String CURRENT_STATE = "CURRENT_STATE";
	public static final String ROUND_STARTED = "ROUND_STARTED";
	public static final String UPGRADE_TURN = "UPGRADE_TURN";
	public static final String PROGRAMMING_STARTED = "PROGRAMMING_STARTED";
	public static final String CHOICE_REQUIRED = "CHOICE_REQUIRED";
	public static final String TIMER_STARTED = "TIMER_STARTED";
	public static final String UPGRADE_USED = "UPGRADE_USED";

	/** The choice activation is waiting on, and who has to make it. */
	public record ChoiceDTO(String username, ChoiceType type,
			List<String> options) {
		public static ChoiceDTO from(Game game, PendingChoice choice) {
			String username = game.getPlayersInGame().stream()
					.filter(player -> player.getId().equals(choice.playerId()))
					.map(player -> player.getUser().getUsername()).findFirst()
					.orElseThrow();
			return new ChoiceDTO(username, choice.type(), choice.options());
		}
	}

	public record UpgradeDTO(UpgradeType type, int cost, UpgradeKind kind) {

		public static UpgradeDTO from(UpgradeCard card) {
			// Convert the upgrade card to the corresponding DTO
			return new UpgradeDTO(card.getType(), card.getType().getCost(),
					card.getType().getKind());
		}
	}

	/**
	 * The phase and register of the round. programmingEndsAt is when the
	 * programming timer runs out, in milliseconds since 1970, and null while it
	 * is not running.
	 */
	public record RoundDTO(Phase phase, Integer currentRegisterIndex,
			long programmingTimeLimitSeconds, Long programmingEndsAt) {
		public static RoundDTO from(Round round) {
			// Convert the round to the corresponding DTO
			return new RoundDTO(round.getCurrentPhase(),
					round.getCurrentRegisterIndex().orElse(null),
					ProgrammingTimer.getTIME_LIMIT().toSeconds(),
					round.getTimer().getEndsAt().map(Instant::toEpochMilli)
							.orElse(null));
		}
	}

	/**
	 * Creates the current player state without round animation frames.
	 */
	public static PlayerGameStateDTO from(String type, Game game,
			Player recipient) {
		return from(type, game, recipient, RoundResult.NONE);
	}

	/**
	 * Creates player state including checkpoint progress, the winner, and the
	 * supplied round animation.
	 */
	public static PlayerGameStateDTO from(String type, Game game,
			Player recipient, RoundResult result) {
		List<PlayerDTO> players = game.getPlayersInGame().stream()
				.map(PlayerDTO::from)
				.sorted(Comparator.comparing(PlayerDTO::username)).toList();

		String winnerUsername = game.getWinner() == null
				? null
				: game.getWinner().getUser().getUsername();

		return new PlayerGameStateDTO(type, game.getGameID(),
				game.getGameStatus(), RoundDTO.from(game.getCurrentRound()),
				BoardDTO.from(game, result), players,
				recipient.getUser().getUsername(),
				PlayerMatDTO.from(recipient.getPlayerMat()),
				game.getUpgradeShop().getCards().stream().map(UpgradeDTO::from)
						.toList(),
				game.getCurrentBuyer()
						.map(player -> player.getUser().getUsername())
						.orElse(null),
				game.getCurrentRound().getPendingChoice()
						.map(choice -> ChoiceDTO.from(game, choice))
						.orElse(null),
				winnerUsername);
	}
}
