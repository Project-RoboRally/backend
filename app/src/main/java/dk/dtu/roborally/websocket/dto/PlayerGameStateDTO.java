package dk.dtu.roborally.websocket.dto;

import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.enums.Phase;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.ProgrammingTimer;
import dk.dtu.roborally.models.game.Round;
import dk.dtu.roborally.models.player.Player;

import java.util.Comparator;
import java.util.List;

/**
 * The full game state as seen by one player: the shared board, round and
 * players, plus that player's private mat. {@code type} identifies the event
 * that caused the message.
 *
 * @author Elias
 */
public record PlayerGameStateDTO(String type, String gameId, GameStatus status,
		RoundDTO round, BoardDTO board, List<PlayerDTO> players, String you,
		PlayerMatDTO playerMat) {

	public static final String GAME_STARTED = "GAME_STARTED";

	public record RoundDTO(Phase phase, Integer currentRegisterIndex,
			long programmingTimeLimitSeconds) {

		public static RoundDTO from(Round round) {
			return new RoundDTO(round.getCurrentPhase(),
					round.getCurrentRegisterIndex().orElse(null),
					ProgrammingTimer.getTIME_LIMIT().toSeconds());
		}
	}

	public static PlayerGameStateDTO from(String type, Game game,
			Player recipient) {
		List<PlayerDTO> players = game.getPlayersInGame().stream()
				.map(PlayerDTO::from)
				.sorted(Comparator.comparing(PlayerDTO::username)).toList();
		return new PlayerGameStateDTO(type, game.getGameID(),
				game.getGameStatus(), RoundDTO.from(game.getCurrentRound()),
				BoardDTO.from(game.getBoard()), players,
				recipient.getUser().getUsername(),
				PlayerMatDTO.from(recipient.getPlayerMat()));
	}
}
