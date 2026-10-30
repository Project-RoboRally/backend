package dk.dtu.roborally.api.dto;

import dk.dtu.roborally.enums.UpgradeType;

/**
 * The upgrade a player wants to buy from the shop.
 */
public record BuyUpgradeDTO(String username, UpgradeType upgrade) {
}
