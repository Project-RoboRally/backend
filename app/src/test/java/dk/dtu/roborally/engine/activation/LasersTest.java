package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.BoardElement;
import dk.dtu.roborally.models.board.elements.LaserEmitter;
import dk.dtu.roborally.models.board.elements.PriorityAntenna;
import dk.dtu.roborally.models.board.elements.Wall;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LasersTest {

	private final Map<Vector2D, Space> spaces = new HashMap<>();

	@BeforeEach
	void buildRow() {
		for (int x = 0; x < 6; x++) {
			spaces.put(new Vector2D(x, 0), new Space(new Vector2D(x, 0)));
		}
	}

	@Test
	void aBoardLaserHitsOnlyTheFirstRobotWithEveryBeam() {
		put(0, new LaserEmitter(new Vector2D(0, 0), Direction.EAST, 2));
		Player first = player("first", 2, Direction.NORTH);
		Player behind = player("behind", 4, Direction.NORTH);

		fire(game(first, behind));

		assertEquals(2, spam(first));
		assertEquals(0, spam(behind));
	}

	@Test
	void aRobotOnTheEmitterGetsHit() {
		put(0, new LaserEmitter(new Vector2D(0, 0), Direction.EAST, 1));
		Player ada = player("ada", 0, Direction.NORTH);

		fire(game(ada));

		assertEquals(1, spam(ada));
	}

	@Test
	void wallsAndTheAntennaStopBoardLasers() {
		put(0, new LaserEmitter(new Vector2D(0, 0), Direction.EAST, 1));
		put(1, new Wall(new Vector2D(1, 0), Direction.EAST));
		put(3, new LaserEmitter(new Vector2D(3, 0), Direction.EAST, 1));
		put(4, new PriorityAntenna());
		Player behindWall = player("wall", 2, Direction.NORTH);
		Player behindAntenna = player("antenna", 5, Direction.NORTH);

		fire(game(behindWall, behindAntenna));

		assertEquals(0, spam(behindWall));
		assertEquals(0, spam(behindAntenna));
	}

	@Test
	void robotsShootTheFirstRobotTheyFace() {
		Player shooter = player("shooter", 0, Direction.EAST);
		Player target = player("target", 2, Direction.NORTH);
		Player behind = player("behind", 3, Direction.NORTH);

		fire(game(shooter, target, behind));

		assertEquals(1, spam(target));
		assertEquals(0, spam(behind));
		assertEquals(0, spam(shooter));
	}

	@Test
	void robotsFacingEachOtherBothGetHit() {
		Player ada = player("ada", 1, Direction.EAST);
		Player bob = player("bob", 4, Direction.WEST);

		fire(game(ada, bob));

		assertEquals(1, spam(ada));
		assertEquals(1, spam(bob));
	}

	@Test
	void wallsStopRobotLasers() {
		put(1, new Wall(new Vector2D(1, 0), Direction.EAST));
		Player shooter = player("shooter", 0, Direction.EAST);
		Player target = player("target", 3, Direction.NORTH);

		fire(game(shooter, target));

		assertEquals(0, spam(target));
	}

	@Test
	void aRebootedRobotCanNotShoot() {
		Player shooter = player("shooter", 0, Direction.EAST);
		Player target = player("target", 2, Direction.NORTH);
		shooter.getRobot().reboot(new Vector2D(0, 0));

		fire(game(shooter, target));

		assertEquals(0, spam(target));
	}

	@Test
	void aDoubleBarrelLaserDealsTwoSpam() {
		Player shooter = player("shooter", 0, Direction.EAST);
		shooter.getUpgrades().install(UpgradeType.DOUBLE_BARREL_LASER, null);
		Player target = player("target", 2, Direction.NORTH);

		fire(game(shooter, target));

		assertEquals(2, spam(target));
	}

	@Test
	void aRailGunHitsEveryRobotInLineThroughWalls() {
		put(1, new Wall(new Vector2D(1, 0), Direction.EAST));
		Player shooter = player("shooter", 0, Direction.EAST);
		shooter.getUpgrades().install(UpgradeType.RAIL_GUN, null);
		Player first = player("first", 2, Direction.NORTH);
		Player second = player("second", 4, Direction.NORTH);

		fire(game(shooter, first, second));

		assertEquals(1, spam(first));
		assertEquals(1, spam(second));
	}

	@Test
	void aRearLaserShootsBackwardsToo() {
		Player shooter = player("shooter", 2, Direction.EAST);
		shooter.getUpgrades().install(UpgradeType.REAR_LASER, null);
		Player ahead = player("ahead", 4, Direction.NORTH);
		Player behind = player("behind", 0, Direction.NORTH);

		fire(game(shooter, ahead, behind));

		assertEquals(1, spam(ahead));
		assertEquals(1, spam(behind));
	}

	@Test
	void aMiniHowitzerCostsEnergyForMoreSpamAndAPush() {
		Player shooter = player("shooter", 0, Direction.EAST);
		shooter.getUpgrades().install(UpgradeType.MINI_HOWITZER, null);
		Player target = player("target", 2, Direction.NORTH);
		Game game = game(shooter, target);
		int energy = shooter.getEnergy();

		pick(game, "shooter", "MINI_HOWITZER");

		assertEquals(3, spam(target));
		assertEquals(new Vector2D(3, 0), target.getRobot().getCurrentSpace());
		assertEquals(energy - 1, shooter.getEnergy());
	}

	@Test
	void pressorAndTractorBeamsMoveTheTarget() {
		Player pusher = player("pusher", 0, Direction.EAST);
		pusher.getUpgrades().install(UpgradeType.PRESSOR_BEAM, null);
		Player pushed = player("pushed", 2, Direction.NORTH);
		Game pushing = game(pusher, pushed);

		pick(pushing, "pusher", "PRESSOR_BEAM");

		assertEquals(new Vector2D(3, 0), pushed.getRobot().getCurrentSpace());

		spaces.clear();
		buildRow();
		Player puller = player("puller", 0, Direction.EAST);
		puller.getUpgrades().install(UpgradeType.TRACTOR_BEAM, null);
		Player pulled = player("pulled", 3, Direction.NORTH);
		Game pulling = game(puller, pulled);

		pick(pulling, "puller", "TRACTOR_BEAM");

		assertEquals(new Vector2D(2, 0), pulled.getRobot().getCurrentSpace());
	}

	/** Fires with the shooter picking one weapon upgrade. */
	private static void pick(Game game, String shooter, String upgrade) {
		game.getCurrentRound().setActivationOrder(game.getPlayersInGame()
				.stream().map(Player::getId).sorted().toList());
		Lasers.fire(game);
		game.getCurrentRound().answer(shooter, upgrade);
		Lasers.fire(game);
		// Only asked again if there is anything left to pick
		if (game.getCurrentRound().getPendingChoice().isPresent()) {
			game.getCurrentRound().answer(shooter, "NONE");
			Lasers.fire(game);
		}
	}

	/** Fires the lasers with everyone in the register's order. */
	private static void fire(Game game) {
		game.getCurrentRound().setActivationOrder(game.getPlayersInGame()
				.stream().map(Player::getId).sorted().toList());
		Lasers.fire(game);
	}

	private void put(int x, BoardElement element) {
		spaces.get(new Vector2D(x, 0)).addBoardElement(element);
	}

	private Game game(Player... players) {
		return new Game("game", new Board(spaces), Set.of(players));
	}

	static long spam(Player player) {
		return player.getPlayerMat().getDiscardPile().stream()
				.filter(card -> card.getName().equals("SPAM")).count();
	}

	static Player player(String name, int x, Direction facing) {
		return new Player(name, new User(name), PlayerColor.RED,
				new Robot(new Vector2D(x, 0), facing));
	}
}
