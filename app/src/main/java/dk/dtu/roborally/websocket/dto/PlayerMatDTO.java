package dk.dtu.roborally.websocket.dto;

import dk.dtu.roborally.models.player.PlayerMat;
import dk.dtu.roborally.models.player.Register;

import java.util.List;

/**
 * The private part of a player's state: their hand, registers and piles. Only
 * sent to the player who owns it.
 *
 * @author Elias, Nicoleta
 */
public record PlayerMatDTO(List<CardDTO> hand, List<RegisterDTO> registers,
		int drawPileSize, List<CardDTO> discardPile) {

	public record RegisterDTO(int index, CardDTO card) {

		public static RegisterDTO from(Register register) {
			// Convert the register to the corresponding DTO
			return new RegisterDTO(register.getIndex(),
					register.getCard() == null
							? null
							: CardDTO.from(register.getCard()));
		}
	}

	public static PlayerMatDTO from(PlayerMat mat) {
		// Convert the player mat to the corresponding DTO
		return new PlayerMatDTO(
				mat.getHand().stream().map(CardDTO::from).toList(),
				mat.getRegisters().stream().map(RegisterDTO::from).toList(),
				mat.getDrawPileSize(),
				mat.getDiscardPile().stream().map(CardDTO::from).toList());
	}
}
