package dk.dtu.roborally.models.player;

import dk.dtu.roborally.enums.UpgradeKind;
import dk.dtu.roborally.enums.UpgradeType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The upgrades a robot has installed. It fits three permanent and three
 * temporary ones. Routines are not kept here, their card goes in the deck.
 *
 * @author Sebastian
 */
public class InstalledUpgrades {

	public static final int SLOTS_PER_KIND = 3;

	private final List<UpgradeType> upgrades = new ArrayList<>();

	public boolean has(UpgradeType type) {
		return upgrades.contains(type);
	}

	public List<UpgradeType> of(UpgradeKind kind) {
		return upgrades.stream().filter(type -> type.getKind() == kind)
				.toList();
	}

	public List<UpgradeType> getAll() {
		return List.copyOf(upgrades);
	}

	/**
	 * Installs an upgrade. When there is no free slot of its kind, replacing
	 * says which installed upgrade of the same kind gets thrown out for it.
	 */
	public void install(UpgradeType type, UpgradeType replacing) {
		Objects.requireNonNull(type);
		if (type.isRoutine()) {
			throw new IllegalArgumentException(
					"Routines go in the deck, not on the mat: " + type);
		}
		if (of(type.getKind()).size() >= SLOTS_PER_KIND) {
			if (replacing == null) {
				throw new IllegalArgumentException(
						"No free " + type.getKind().name().toLowerCase()
								+ " slot, pick an upgrade to replace");
			}
			if (replacing.getKind() != type.getKind() || !has(replacing)) {
				throw new IllegalArgumentException(
						"Can not replace " + replacing + " with " + type);
			}
			upgrades.remove(replacing);
		}
		upgrades.add(type);
	}

	/** Takes an upgrade off, like a used temporary one. */
	public void remove(UpgradeType type) {
		if (!upgrades.remove(type)) {
			throw new IllegalArgumentException("Not installed: " + type);
		}
	}

	/** Puts back upgrades loaded from storage. */
	public void restore(List<UpgradeType> types) {
		upgrades.clear();
		upgrades.addAll(types);
	}
}
