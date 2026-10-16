package dk.dtu.roborally.api.dto.game;

import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.enums.Phase;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.Round;

public record GameDTO(String gameId, GameStatus status, int roundNumber,
		Phase currentPhase, Integer currentRegister) {

	public static GameDTO from(Game game) {
		Round round = game.getCurrentRound();

		return new GameDTO(game.getGameID(), game.getGameStatus(),
				round.getRoundNumber(), round.getCurrentPhase(),
				round.getCurrentRegisterNumber().orElse(null));
	}
}
