package dk.dtu.roborally.models.game;

import dk.dtu.roborally.models.game.RoundResult.PlayedCard;
import dk.dtu.roborally.models.game.RoundResult.RegisterReveal;
import dk.dtu.roborally.models.player.RobotView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * What has happened so far in the activation phase: the robot poses to animate
 * and the cards played in each register.
 *
 * @author Sebastian
 */
public class ActivationLog {

	private final List<List<RobotView>> steps = new ArrayList<>();
	private final Map<Integer, List<PlayedCard>> cards = new TreeMap<>();

	public int stepCount() {
		return steps.size();
	}

	public void addStep(List<RobotView> step) {
		steps.add(List.copyOf(step));
	}

	public void addCard(int register, PlayedCard card) {
		cards.computeIfAbsent(register, ignored -> new ArrayList<>()).add(card);
	}

	public RoundResult toResult() {
		List<RegisterReveal> registers = new ArrayList<>();
		cards.forEach((register, played) -> registers
				.add(new RegisterReveal(register, List.copyOf(played))));
		return new RoundResult(List.copyOf(steps), registers);
	}
}
