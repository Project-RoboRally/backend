package dk.dtu.roborally.api.dto;

import dk.dtu.roborally.enums.UpgradeType;

import java.util.List;

/**
 * A player using one of their upgrades during programming. cards names the hand
 * cards the upgrade works on, for the upgrades that need them.
 */
public record UseUpgradeDTO(String username, UpgradeType upgrade,
		List<String> cards) {
}
