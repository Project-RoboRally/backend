package dk.dtu.roborally.websocket.dto;

import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.player.InstalledUpgrades;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.PlayerMat;
import dk.dtu.roborally.models.player.Register;

import java.util.List;

/**
 * The private part of a player's state: their hand, registers and piles. Only
 * sent to the player who owns it. {@code usedThisRound} lists once-per-round
 * upgrades already spent, so the client can stop offering them.
 *
 * @author Elias, Nicoleta
 */
public record PlayerMatDTO(List<CardDTO> hand, List<RegisterDTO> registers,
		int drawPileSize, List<CardDTO> discardPile, int cardsToPutBack,
		List<UpgradeType> usedThisRound) {

	public record RegisterDTO(int index, CardDTO card) {

		public static RegisterDTO from(Register register) {
			// Convert the register to the corresponding DTO
			return new RegisterDTO(register.getIndex(),
					register.getCard() == null
							? null
							: CardDTO.from(register.getCard()));
		}
	}

	public static PlayerMatDTO from(Player player) {
		PlayerMat mat = player.getPlayerMat();
		InstalledUpgrades upgrades = player.getUpgrades();
		return new PlayerMatDTO(
				mat.getHand().stream().map(CardDTO::from).toList(),
				mat.getRegisters().stream().map(RegisterDTO::from).toList(),
				mat.getDrawPileSize(),
				mat.getDiscardPile().stream().map(CardDTO::from).toList(),
				mat.getCardsToPutBack(),
				List.copyOf(upgrades.getUsedThisRound()));
	}
}
