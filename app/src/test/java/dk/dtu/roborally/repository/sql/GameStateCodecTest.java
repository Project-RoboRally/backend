package dk.dtu.roborally.repository.sql;

import dk.dtu.roborally.enums.ChoiceType;
import dk.dtu.roborally.enums.RegisterStage;
import dk.dtu.roborally.loaders.JsonLoader;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.PendingChoice;
import dk.dtu.roborally.models.game.Round;
import dk.dtu.roborally.models.lobby.GameLobby;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

	private Game reload() {
		return codec.toGame(codec.toJson(game), lobby.getLevel(), lobby);
	}
}
