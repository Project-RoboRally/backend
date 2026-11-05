package dk.dtu.roborally.models.game;

import dk.dtu.roborally.enums.ChoiceType;
import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.enums.Phase;
import dk.dtu.roborally.enums.RegisterStage;
import dk.dtu.roborally.enums.UpgradeType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

	/** Board lasers already fired this register. */
	@Getter
	@Setter
	private boolean boardLasersFired;
	/** How many robots have fired their weapons this register. */
	@Getter
	private int shotsFired;

	/** Upgrades each player used at the start of this register. */
	private final Map<String, List<UpgradeType>> usedAtRegisterStart = new HashMap<>();
	/** Players who are done picking upgrades at the start of this register. */
	private final Set<String> startDone = new HashSet<>();

	/** Whether the player whose turn it is has finished using upgrades. */
	@Getter
	@Setter
	private boolean turnDone;
	/** A turn upgrade that was picked and still needs its direction. */
	private UpgradeType pickedUpgrade;
	/** Hack was used this turn, the card gets played twice. */
	@Getter
	@Setter
	private boolean hacked;
	/** Refresh changed this turn's card to this. */
	private CommandInstruction refreshedTo;
	/** Answers about this turn's card, kept for when Hack plays it again. */
	private final Map<ChoiceType, String> turnAnswers = new EnumMap<>(
			ChoiceType.class);

	/** Player ids of rebooted robots that still have to pick a facing. */
	private final List<String> rebootsToFace = new ArrayList<>();

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
		boardLasersFired = false;
		shotsFired = 0;
		currentRegisterIndex = register;
		stage = RegisterStage.CARDS;
		activationOrder.clear();
		activationOrder.addAll(order);
		activated = 0;
	}

	/**
	 * Starts a register with the upgrades asked about first. The order is set
	 * once everyone has answered, see {@link #setActivationOrder}.
	 */
	public void beginRegister(int register) {
		startRegister(register, List.of());
		stage = RegisterStage.START;
		usedAtRegisterStart.clear();
		startDone.clear();
	}

	/** Sets who goes in what order this register, and the cards can start. */
	public void setActivationOrder(List<String> order) {
		activationOrder.clear();
		activationOrder.addAll(order);
		activated = 0;
		stage = RegisterStage.CARDS;
	}

	public void useAtRegisterStart(String playerId, UpgradeType type) {
		usedAtRegisterStart
				.computeIfAbsent(playerId, ignored -> new ArrayList<>())
				.add(type);
	}

	public List<UpgradeType> getUsedAtRegisterStart(String playerId) {
		return List
				.copyOf(usedAtRegisterStart.getOrDefault(playerId, List.of()));
	}

	public Map<String, List<UpgradeType>> getUsedAtRegisterStart() {
		return Map.copyOf(usedAtRegisterStart);
	}

	public void markStartDone(String playerId) {
		startDone.add(playerId);
	}

	public boolean isStartDone(String playerId) {
		return startDone.contains(playerId);
	}

	public Set<String> getStartDone() {
		return Set.copyOf(startDone);
	}

	/** Whether the player put up a Deflector Shield this register. */
	public boolean isShielded(String playerId) {
		return getUsedAtRegisterStart(playerId)
				.contains(UpgradeType.DEFLECTOR_SHIELD);
	}

	/** Puts back the register start upgrades loaded from storage. */
	public void restoreRegisterStart(Map<String, List<UpgradeType>> used,
			Set<String> done) {
		usedAtRegisterStart.clear();
		used.forEach((id, types) -> usedAtRegisterStart.put(id,
				new ArrayList<>(types)));
		startDone.clear();
		startDone.addAll(done);
	}

	public void markShotFired() {
		shotsFired++;
	}

	/** Puts back how far the lasers got, loaded from storage. */
	public void restoreLasers(boolean boardLasersFired, int shotsFired) {
		this.boardLasersFired = boardLasersFired;
		this.shotsFired = shotsFired;
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
		turnDone = false;
		pickedUpgrade = null;
		hacked = false;
		refreshedTo = null;
		turnAnswers.clear();
	}

	public Optional<String> getTurnAnswer(ChoiceType type) {
		return Optional.ofNullable(turnAnswers.get(type));
	}

	public void setTurnAnswer(ChoiceType type, String answer) {
		turnAnswers.put(type, answer);
	}

	public Map<ChoiceType, String> getTurnAnswers() {
		return Map.copyOf(turnAnswers);
	}

	public Optional<CommandInstruction> getRefreshedTo() {
		return Optional.ofNullable(refreshedTo);
	}

	public void setRefreshedTo(CommandInstruction instruction) {
		refreshedTo = instruction;
	}

	public Optional<UpgradeType> getPickedUpgrade() {
		return Optional.ofNullable(pickedUpgrade);
	}

	public void setPickedUpgrade(UpgradeType type) {
		pickedUpgrade = type;
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

	public void addRebootToFace(String playerId) {
		rebootsToFace.add(playerId);
	}

	public List<String> getRebootsToFace() {
		return List.copyOf(rebootsToFace);
	}

	public void removeRebootToFace(String playerId) {
		rebootsToFace.remove(playerId);
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

	/** Puts back the robots waiting to pick a facing, loaded from storage. */
	public void restoreRebootsToFace(List<String> playerIds) {
		rebootsToFace.clear();
		rebootsToFace.addAll(playerIds);
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
