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
	}
}
