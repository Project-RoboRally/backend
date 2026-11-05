package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.cards.DamageCard;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DamageTest {

	@Test
	void damageGoesToTheDiscardPileOneCardAtATime() {
		Player ada = new Player("ada", new User("ada"), PlayerColor.RED,
				new Robot(new Vector2D(0, 0), Direction.NORTH));
		Game game = new Game("game", new Board(Map.of()), Set.of(ada));

		Damage.take(game, ada, DamageInstruction.SPAM, 2);

		assertEquals(2, ada.getPlayerMat().getDiscardPile().size());
		assertTrue(ada.getPlayerMat().getDiscardPile().stream()
				.allMatch(card -> card instanceof DamageCard damage
						&& damage.getInstruction() == DamageInstruction.SPAM));
		assertEquals(36, game.getDamagePiles().size(DamageInstruction.SPAM));
	}
}
