package dk.dtu.roborally.models.player;

import dk.dtu.roborally.enums.UpgradeKind;
import dk.dtu.roborally.enums.UpgradeType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InstalledUpgradesTest {

	@Test
	void holdsThreeOfEachKind() {
		InstalledUpgrades upgrades = new InstalledUpgrades();
		upgrades.install(UpgradeType.BRAKES, null);
		upgrades.install(UpgradeType.FIREWALL, null);
		upgrades.install(UpgradeType.RAIL_GUN, null);
		upgrades.install(UpgradeType.ZOOP, null);

		assertEquals(3, upgrades.of(UpgradeKind.PERMANENT).size());
		assertEquals(List.of(UpgradeType.ZOOP),
				upgrades.of(UpgradeKind.TEMPORARY));
		assertThrows(IllegalArgumentException.class,
				() -> upgrades.install(UpgradeType.HOVER_UNIT, null));
	}

	@Test
	void aFullKindNeedsAnUpgradeOfTheSameKindToReplace() {
		InstalledUpgrades upgrades = new InstalledUpgrades();
		upgrades.install(UpgradeType.BRAKES, null);
		upgrades.install(UpgradeType.FIREWALL, null);
		upgrades.install(UpgradeType.RAIL_GUN, null);
		upgrades.install(UpgradeType.ZOOP, null);

		assertThrows(IllegalArgumentException.class, () -> upgrades
				.install(UpgradeType.HOVER_UNIT, UpgradeType.ZOOP));
		upgrades.install(UpgradeType.HOVER_UNIT, UpgradeType.BRAKES);

		assertEquals(
				List.of(UpgradeType.FIREWALL, UpgradeType.RAIL_GUN,
						UpgradeType.ZOOP, UpgradeType.HOVER_UNIT),
				upgrades.getAll());
	}

	@Test
	void routinesAreNotInstalled() {
		assertThrows(IllegalArgumentException.class,
				() -> new InstalledUpgrades().install(UpgradeType.SPEED_ROUTINE,
						null));
	}
}
