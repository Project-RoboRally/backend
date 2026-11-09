package dk.dtu.roborally.enums;

import lombok.Getter;

/**
 * Every upgrade card in the game, one copy each. Costs and effects are the ones
 * printed on the cards in the rulebook. Routines are temporary upgrades that
 * put their programming card in your discard pile when you buy them.
 *
 * @author Sebastian
 */
@Getter
public enum UpgradeType {
	// Permanent
	ADMIN_PRIVILEGE(3, UpgradeKind.PERMANENT), // Priority for one register
	BLUE_SCREEN_OF_DEATH(4, UpgradeKind.PERMANENT), // Worm instead of SPAM
	BRAKES(3, UpgradeKind.PERMANENT), // Move 1 may be Move 0
	CACHE_MEMORY(4, UpgradeKind.PERMANENT), // Hand cards back on the deck
	CORRUPTION_WAVE(4, UpgradeKind.PERMANENT), // SPAM on top of their deck
	CRAB_LEGS(5, UpgradeKind.PERMANENT), // Move 1 sideways
	DEFLECTOR_SHIELD(2, UpgradeKind.PERMANENT), // Pay 1 to stop robot lasers
	DEFRAG_GIZMO(5, UpgradeKind.PERMANENT), // Get rid of a damage card
	DOUBLE_BARREL_LASER(2, UpgradeKind.PERMANENT), // One more SPAM
	FIREWALL(3, UpgradeKind.PERMANENT), // No SPAM when rebooting
	HOVER_UNIT(1, UpgradeKind.PERMANENT), // Pass over pits
	MEMORY_STICK(3, UpgradeKind.PERMANENT), // Draw one more card
	MINI_HOWITZER(2, UpgradeKind.PERMANENT), // Pay 1 for 2 SPAM and a push
	MODULAR_CHASSIS(1, UpgradeKind.PERMANENT), // Swap upgrades on a push
	PRESSOR_BEAM(3, UpgradeKind.PERMANENT), // Push who you shoot
	RAIL_GUN(2, UpgradeKind.PERMANENT), // Shoot through walls and robots
	RAMMING_GEAR(2, UpgradeKind.PERMANENT), // SPAM when you push
	REAR_LASER(2, UpgradeKind.PERMANENT), // Shoot backwards too
	SCRAMBLER(3, UpgradeKind.PERMANENT), // Scramble their next register
	SIDE_ARMS(3, UpgradeKind.PERMANENT), // Push left or right
	TELEPORTER(3, UpgradeKind.PERMANENT), // Pay 1 to ignore obstacles
	TRACTOR_BEAM(3, UpgradeKind.PERMANENT), // Pull who you shoot
	TROJAN_NEEDLER(3, UpgradeKind.PERMANENT), // Trojan instead of SPAM
	VIRUS_MODULE(2, UpgradeKind.PERMANENT), // Virus when you push
	// Temporary
	BOINK(1, UpgradeKind.TEMPORARY), // Move to an adjacent space
	ENERGY_ROUTINE(3, CommandInstruction.ENERGY_ROUTINE), // Extra Power Up
	HACK(1, UpgradeKind.TEMPORARY), // Run this register again
	MANUAL_SORT(1, UpgradeKind.TEMPORARY), // Priority this register
	MEMORY_SWAP(1, UpgradeKind.TEMPORARY), // Draw 3, put 3 back
	REBOOT(1, UpgradeKind.TEMPORARY), // Reboot without damage
	RECHARGE(0, UpgradeKind.TEMPORARY), // Gain 3 energy
	RECOMPILE(1, UpgradeKind.TEMPORARY), // New hand
	REFRESH(2, UpgradeKind.TEMPORARY), // Change this register
	REPEAT_ROUTINE(3, CommandInstruction.REPEAT_ROUTINE), // Extra Again
	SANDBOX_ROUTINE(5, CommandInstruction.SANDBOX_ROUTINE), // Any move or turn
	SPAM_BLOCKER(3, UpgradeKind.TEMPORARY), // Swap SPAM in hand
	SPAM_FOLDER(2, CommandInstruction.SPAM_FOLDER), // Gets rid of a SPAM
	SPEED_ROUTINE(3, CommandInstruction.SPEED_ROUTINE), // Extra Move 3
	WEASEL_ROUTINE(3, CommandInstruction.WEASEL_ROUTINE), // Any turn
	ZOOP(1, UpgradeKind.TEMPORARY); // Face any direction

	private final int cost;
	private final UpgradeKind kind;
	/** The card a routine adds to your deck, null for other upgrades. */
	private final CommandInstruction routine;

	UpgradeType(int cost, UpgradeKind kind) {
		this.cost = cost;
		this.kind = kind;
		this.routine = null;
	}

	UpgradeType(int cost, CommandInstruction routine) {
		this.cost = cost;
		this.kind = UpgradeKind.TEMPORARY;
		this.routine = routine;
	}

	public boolean isRoutine() {
		return routine != null;
	}
}
