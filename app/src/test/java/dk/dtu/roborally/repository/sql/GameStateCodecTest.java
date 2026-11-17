package dk.dtu.roborally.repository.sql;

import dk.dtu.roborally.engine.activation.Damage;
import dk.dtu.roborally.enums.ChoiceType;
import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.RegisterStage;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.loaders.LevelLoader;
import dk.dtu.roborally.loaders.JsonLoader;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.EnergySpace;
import dk.dtu.roborally.models.cards.ProgrammableCard;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.PendingChoice;
import dk.dtu.roborally.models.game.Round;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.models.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStateCodecTest {

	private final GameStateCodec codec = new GameStateCodec();
	private GameLobby lobby;
	private Game game;

	@BeforeEach
	void setUp() {
		User owner = new User("owner");
		lobby = new GameLobby(JsonLoader.loadConfig(), "Codec lobby", owner);
		lobby.addUserToLobby(new User("other"));
		lobby.setReady(
				lobby.getUsersInLobby().stream().filter(user -> user != owner)
						.findFirst().orElseThrow().getId(),
				true);
		game = lobby.startGame(new Random(1));
	}

	@Test
	void keepsActivationProgress() {
		List<String> order = game.getPlayersInGame().stream()
				.map(player -> player.getId()).sorted().toList();
		game.getCurrentRound().startRegister(3, order);
		game.getCurrentRound().markActivated();

		Round loaded = reload().getCurrentRound();

		assertEquals(Optional.of(3), loaded.getCurrentRegisterIndex());
		assertEquals(RegisterStage.CARDS, loaded.getStage());
		assertEquals(order, loaded.getActivationOrder());
		assertEquals(Optional.of(order.get(1)), loaded.getNextToActivate());
	}

	@Test
	void aRoundOutsideActivationLoadsAsNotActivating() {
		assertFalse(reload().getCurrentRound().isActivating());
	}

	@Test
	void keepsAPendingChoiceAndItsAnswer() {
		String playerId = game.getPlayersInGame().iterator().next().getId();
		PendingChoice choice = new PendingChoice(playerId,
				ChoiceType.SANDBOX_ACTION, List.of("MOVE_1", "U_TURN"));
		game.getCurrentRound().ask(choice);

		assertEquals(Optional.of(choice),
				reload().getCurrentRound().getPendingChoice());

		game.getCurrentRound().answer(playerId, "U_TURN");

		assertEquals(Optional.of("U_TURN"),
				reload().getCurrentRound().ask(choice));
	}

	@Test
	void keepsExecutedInstructionsFromAHackedRegister() {
		String playerId = game.getPlayersInGame().iterator().next().getId();
		game.getCurrentRound().startRegister(1, List.of(playerId));
		game.getCurrentRound().recordExecuted(playerId, 0,
				CommandInstruction.LEFT_TURN);
		game.getCurrentRound().recordExecuted(playerId, 0,
				CommandInstruction.LEFT_TURN);

		assertEquals(
				List.of(CommandInstruction.LEFT_TURN,
						CommandInstruction.LEFT_TURN),
				reload().getCurrentRound().getExecutedInstructions(playerId,
						0));
	}

	@Test
	void keepsDamageCardsAndPileSizes() {
		Player player = game.getPlayersInGame().iterator().next();
		Damage.take(game, player, DamageInstruction.VIRUS, 2);
		player.getPlayerMat().getRegisters().get(2).setDamaged(true);

		Game loaded = reload();
		Player same = loaded.getPlayersInGame().stream()
				.filter(other -> other.getId().equals(player.getId()))
				.findFirst().orElseThrow();

		assertEquals(List.of("VIRUS", "VIRUS"),
				same.getPlayerMat().getDiscardPile().stream()
						.map(ProgrammableCard::getName).toList());
		assertEquals(16, loaded.getDamagePiles().size(DamageInstruction.VIRUS));
	}

	@Test
	void robotsRememberWhereTheyStarted() {
		Player player = game.getPlayersInGame().iterator().next();
		Vector2D start = player.getRobot().getStartSpace();
		player.getRobot().moveTo(new Vector2D(3, 3));

		Player loaded = reload().getPlayersInGame().stream()
				.filter(other -> other.getId().equals(player.getId()))
				.findFirst().orElseThrow();

		assertEquals(start, loaded.getRobot().getStartSpace());
		assertEquals(new Vector2D(3, 3), loaded.getRobot().getCurrentSpace());
	}

	@Test
	void keepsWhichEnergySpacesAreEmpty() {
		EnergySpace battery = game.getBoard().getElements(EnergySpace.class)
				.get(0);
		battery.setCubeAmount(0);

		Game loaded = codec.toGame(codec.toJson(game),
				LevelLoader.load("levels/level_0.json"), lobby);

		assertEquals(0,
				loaded.getBoard().getSpace(battery.getPosition())
						.getBoardElements().stream()
						.filter(EnergySpace.class::isInstance)
						.map(EnergySpace.class::cast).findFirst().orElseThrow()
						.getCubeAmount());
	}

	@Test
	void keepsTheProgrammingDeadline() {
		Instant now = Instant.ofEpochMilli(1_800_000_000_000L);
		game.getCurrentRound().getTimer().start(now);

		assertEquals(Optional.of(now.plusSeconds(30)),
				reload().getCurrentRound().getTimer().getEndsAt());
	}

	@Test
	void keepsTheRoundNumber() {
		game.getCurrentRound().beginNext();
		game.getCurrentRound().beginNext();

		assertEquals(2, reload().getCurrentRound().getNumber());
	}

	@Test
	void keepsInstalledUpgrades() {
		Player player = game.getPlayersInGame().iterator().next();
		player.getUpgrades().install(UpgradeType.RAIL_GUN, null);
		player.getUpgrades().install(UpgradeType.ZOOP, null);

		Player loaded = reload().getPlayersInGame().stream()
				.filter(other -> other.getId().equals(player.getId()))
				.findFirst().orElseThrow();

		assertEquals(List.of(UpgradeType.RAIL_GUN, UpgradeType.ZOOP),
				loaded.getUpgrades().getAll());
	}

	@Test
	void keepsCardsOwedFromAMemorySwap() {
		Player player = game.getPlayersInGame().iterator().next();
		player.getPlayerMat().swapCards(3);

		Player loaded = reload().getPlayersInGame().stream()
				.filter(other -> other.getId().equals(player.getId()))
				.findFirst().orElseThrow();

		assertEquals(3, loaded.getPlayerMat().getCardsToPutBack());
	}

	@Test
	void keepsUpgradesUsedAtTheStartOfTheRegister() {
		String playerId = game.getPlayersInGame().iterator().next().getId();
		game.getCurrentRound().beginRegister(1);
		game.getCurrentRound().useAtRegisterStart(playerId,
				UpgradeType.DEFLECTOR_SHIELD);
		game.getCurrentRound().markStartDone(playerId);

		Round loaded = reload().getCurrentRound();

		assertEquals(RegisterStage.START, loaded.getStage());
		assertTrue(loaded.isShielded(playerId));
		assertTrue(loaded.isStartDone(playerId));
	}

	private Game reload() {
		return codec.toGame(codec.toJson(game), lobby.getLevel(), lobby);
	}
}
