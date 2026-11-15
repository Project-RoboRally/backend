package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.ChoiceType;
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DamageTest {

	@Test
	void damageGoesToTheDiscardPileOneCardAtATime() {
		Player ada = player("ada");
		Game game = game(ada);

		Damage.take(game, ada, DamageInstruction.SPAM, 2);

		assertEquals(2, ada.getPlayerMat().getDiscardPile().size());
		assertTrue(ada.getPlayerMat().getDiscardPile().stream()
				.allMatch(card -> card instanceof DamageCard damage
						&& damage.getInstruction() == DamageInstruction.SPAM));
		assertEquals(36, game.getDamagePiles().size(DamageInstruction.SPAM));
	}

	@Test
	void outsideActivationAnEmptyPileTakesFromTheBiggestOtherPile() {
		Player ada = player("ada");
		Game game = game(ada);
		game.getDamagePiles().restore(Map.of(DamageInstruction.SPAM, 0,
				DamageInstruction.TROJAN_HORSE, 2, DamageInstruction.WORM, 5,
				DamageInstruction.VIRUS, 1));

		Damage.take(game, ada, DamageInstruction.SPAM, 1);

		assertEquals(List.of("WORM"), ada.getPlayerMat().getDiscardPile()
				.stream().map(card -> card.getName()).toList());
		assertEquals(4, game.getDamagePiles().size(DamageInstruction.WORM));
	}

	@Test
	void duringActivationThePlayerPicksAnotherType() {
		Player ada = player("ada");
		Game game = game(ada);
		game.getCurrentRound().startActivation();
		game.getCurrentRound().startRegister(0, List.of(ada.getId()));
		game.getDamagePiles().restore(Map.of(DamageInstruction.SPAM, 0,
				DamageInstruction.TROJAN_HORSE, 2, DamageInstruction.WORM, 5,
				DamageInstruction.VIRUS, 1));

		Damage.take(game, ada, DamageInstruction.SPAM, 1);

		assertTrue(ada.getPlayerMat().getDiscardPile().isEmpty());
		assertEquals(1, game.getCurrentRound().getDamageDraws().size());

		assertFalse(Damage.giveQueued(game));
		var choice = game.getCurrentRound().getPendingChoice().orElseThrow();
		assertEquals(ChoiceType.DAMAGE_TYPE, choice.type());
		assertEquals(List.of("TROJAN_HORSE", "WORM", "VIRUS"),
				choice.options());

		game.getCurrentRound().answer(ada.getId(), "VIRUS");
		assertTrue(Damage.giveQueued(game));

		assertEquals(List.of("VIRUS"), ada.getPlayerMat().getDiscardPile()
				.stream().map(card -> card.getName()).toList());
		assertTrue(game.getCurrentRound().getDamageDraws().isEmpty());
	}

	private static Player player(String name) {
		return new Player(name, new User(name), PlayerColor.RED,
				new Robot(new Vector2D(0, 0), Direction.NORTH));
	}

	private static Game game(Player player) {
		return new Game("game", new Board(Map.of()), Set.of(player));
	}
}
