package dk.dtu.roborally.api.dto;

import dk.dtu.roborally.enums.Phase;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.Round;

public record GameDTO(
        String gameId,
        int roundNumber,
        Phase currentPhase,
        Integer currentRegister
) {

    public static GameDTO from(Game game) {
        Round round = game.getCurrentRound();

        return new GameDTO(
                game.getGameID(),
                round.getRoundNumber(),
                round.getCurrentPhase(),
                round.getCurrentRegisterNumber().orElse(null)
        );
    }
}