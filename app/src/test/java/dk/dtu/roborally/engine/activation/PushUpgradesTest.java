package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PushUpgradesTest {

	private Player pusher;
	private Player target;
	private Game game;

	@BeforeEach
	void setUp() {
		pusher = player("pusher");
		target = player("target");
		game = new Game("game", new Board(Map.of()), Set.of(pusher, target));
	}

	@Test
	void rammingGearDealsASpam() {
		pusher.getUpgrades().install(UpgradeType.RAMMING_GEAR, null);

		PushUpgrades.pushed(game, pusher, target);

		assertEquals(List.of("SPAM"), discarded(target));
	}

	@Test
	void theRammingSpamCanBeAWormOrATrojan() {
		pusher.getUpgrades().install(UpgradeType.RAMMING_GEAR, null);
		pusher.getUpgrades().install(UpgradeType.TROJAN_NEEDLER, null);

		PushUpgrades.pushed(game, pusher, target);
		assertEquals(List.of("TROJAN_HORSE"), discarded(target));

		pusher.getUpgrades().install(UpgradeType.BLUE_SCREEN_OF_DEATH, null);
		PushUpgrades.pushed(game, pusher, target);
		assertEquals(List.of("TROJAN_HORSE", "WORM"), discarded(target));
	}

	@Test
	void aVirusModuleGivesAVirus() {
		pusher.getUpgrades().install(UpgradeType.VIRUS_MODULE, null);

		PushUpgrades.pushed(game, pusher, target);

		assertEquals(List.of("VIRUS"), discarded(target));
	}

	@Test
	void withoutUpgradesAPushDoesNoDamage() {
		PushUpgrades.pushed(game, pusher, target);

		assertTrue(target.getPlayerMat().getDiscardPile().isEmpty());
	}

	private static List<String> discarded(Player player) {
		return player.getPlayerMat().getDiscardPile().stream()
				.map(card -> card.getName()).toList();
	}

	private static Player player(String name) {
		return new Player(name, new User(name), PlayerColor.RED,
				new Robot(new Vector2D(0, 0), Direction.NORTH));
	}
}
