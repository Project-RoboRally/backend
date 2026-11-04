package dk.dtu.roborally.models.game;

import dk.dtu.roborally.models.cards.ProgrammableCard;
import dk.dtu.roborally.models.player.RobotView;

import java.util.List;

/**
 * What happened in an activated round: the robot frames to animate, and the
 * cards played in each register.
 *
 * @author Sebastian
 */
public record RoundResult(List<List<RobotView>> steps,
		List<RegisterReveal> registers) {

	public static final RoundResult NONE = new RoundResult(List.of(),
			List.of());

	/** The cards of one register, in the order they activated. */
	public record RegisterReveal(int register, List<PlayedCard> cards) {
	}

	/** One player's card and the frames in steps it produced. */
	public record PlayedCard(String username, ProgrammableCard card,
			int firstStep, int stepCount) {
	}
}
