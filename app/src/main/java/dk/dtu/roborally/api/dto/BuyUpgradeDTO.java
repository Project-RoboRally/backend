package dk.dtu.roborally.api.dto;

import dk.dtu.roborally.enums.UpgradeType;

/**
 * The upgrade a player wants to buy from the shop. replace is only needed when
 * all three slots of that kind are taken, and names the installed upgrade to
 * throw out.
 *
 * @author Sebastian
 */
public record BuyUpgradeDTO(UpgradeType upgrade, UpgradeType replace) {
}
