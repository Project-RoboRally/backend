package dk.dtu.roborally.models.cards;

import dk.dtu.roborally.enums.UpgradeType;
import lombok.Getter;

import java.util.Objects;

/**
 * Represents an upgrade card that can be bought in the upgrade shop.
 *
 * @author Sebastian
 */
@Getter
public class UpgradeCard extends Card {
	private final UpgradeType type;

	public UpgradeCard(UpgradeType type) {
		this.type = Objects.requireNonNull(type);
	}
}
