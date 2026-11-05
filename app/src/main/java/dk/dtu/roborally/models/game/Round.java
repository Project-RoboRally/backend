package dk.dtu.roborally.models.game;

import dk.dtu.roborally.enums.Phase;
import dk.dtu.roborally.enums.RegisterStage;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents an active or ended round
 *
 * @author August, Sebastian
 */

public class Round {
	@Getter
	@Setter
	private Phase currentPhase = Phase.UPGRADE;

	@Getter
	private final ProgrammingTimer timer = new ProgrammingTimer();

	private Integer currentRegisterIndex;

	@Getter
	@Setter
	private RegisterStage stage;

	/** Player ids in the order they activate this register. */
	private final List<String> activationOrder = new ArrayList<>();
	private int activated;

	@Getter
	private ActivationLog log = new ActivationLog();

	private PendingChoice choice;
	private String answer;

	/** The register being activated, empty outside the activation phase. */
	public Optional<Integer> getCurrentRegisterIndex() {
		return Optional.ofNullable(currentRegisterIndex);
	}

	public boolean isActivating() {
		return currentRegisterIndex != null;
	}

	/** Clears what happened in the last activation phase. */
	public void startActivation() {
		log = new ActivationLog();
	}

	/** What happened since this was last called, so nothing is sent twice. */
	public ActivationLog takeLog() {
		ActivationLog taken = log;
		log = new ActivationLog();
		return taken;
	}

	/**
	 * Starts a register with its cards stage. The order is fixed here, so
	 * robots moving during the register do not change who goes next.
	 */
	public void startRegister(int register, List<String> order) {
		currentRegisterIndex = register;
		stage = RegisterStage.CARDS;
		activationOrder.clear();
		activationOrder.addAll(order);
		activated = 0;
	}

	public List<String> getActivationOrder() {
		return List.copyOf(activationOrder);
	}

	/** The next player to play their card this register, if any are left. */
	public Optional<String> getNextToActivate() {
		if (activated >= activationOrder.size()) {
			return Optional.empty();
		}
		return Optional.of(activationOrder.get(activated));
	}

	public void markActivated() {
		activated++;
	}

	public int getActivatedCount() {
		return activated;
	}

	/** The decision activation is waiting on, if any. */
	public Optional<PendingChoice> getPendingChoice() {
		return answer == null ? Optional.ofNullable(choice) : Optional.empty();
	}

	/**
	 * The answer to the given choice if the player already gave it. Otherwise
	 * activation starts waiting for it, and the step asking should stop and be
	 * run again once the answer is in.
	 */
	public Optional<String> ask(PendingChoice question) {
		if (question.equals(choice) && answer != null) {
			String given = answer;
			choice = null;
			answer = null;
			return Optional.of(given);
		}
		choice = question;
		answer = null;
		return Optional.empty();
	}

	/** Stores a player's answer to the pending choice. */
	public void answer(String playerId, String value) {
		PendingChoice pending = getPendingChoice().orElseThrow(
				() -> new IllegalStateException("Nothing to choose right now"));
		if (!pending.playerId().equals(playerId)) {
			throw new IllegalStateException("It is not your choice to make");
		}
		if (!pending.options().contains(value)) {
			throw new IllegalArgumentException("Not an option: " + value);
		}
		answer = value;
	}

	/** The answer given but not used yet, kept so storage can restore it. */
	public Optional<String> getAnswer() {
		return Optional.ofNullable(answer);
	}

	/** The choice an unused answer belongs to. */
	public Optional<PendingChoice> getAnsweredChoice() {
		return answer == null ? Optional.empty() : Optional.of(choice);
	}

	/** Puts back a choice loaded from storage. */
	public void restoreChoice(PendingChoice choice, String answer) {
		this.choice = choice;
		this.answer = answer;
	}

	/** Puts back activation progress loaded from storage. */
	public void restoreActivation(Integer register, RegisterStage stage,
			List<String> order, int activated) {
		this.currentRegisterIndex = register;
		this.stage = stage;
		activationOrder.clear();
		activationOrder.addAll(order);
		this.activated = activated;
	}

	public void finishActivation() {
		currentRegisterIndex = null;
		stage = null;
		activationOrder.clear();
		activated = 0;
		choice = null;
		answer = null;
	}
}
