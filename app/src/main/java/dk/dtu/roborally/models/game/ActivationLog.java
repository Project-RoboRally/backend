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
 * @author Sebastian, Elias
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

	/**
	 * Drops animation frames that have already been published, but keeps the
	 * revealed cards so later pauses still show every register so far.
	 */
	public void clearSteps() {
		steps.clear();
	}

	public RoundResult toResult() {
		List<RegisterReveal> registers = new ArrayList<>();
		cards.forEach((register, played) -> registers
				.add(new RegisterReveal(register, List.copyOf(played))));
		return new RoundResult(List.copyOf(steps), registers);
	}

	/** Snapshot of the current log, so the live log can clear steps safely. */
	public ActivationLog copy() {
		ActivationLog copy = new ActivationLog();
		for (List<RobotView> step : steps) {
			copy.addStep(step);
		}
		cards.forEach((register, played) -> played
				.forEach(card -> copy.addCard(register, card)));
		return copy;
	}
}
