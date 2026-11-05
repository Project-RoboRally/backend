package dk.dtu.roborally.repository.sql;

import com.google.gson.Gson;

import dk.dtu.roborally.enums.ChoiceType;
import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.enums.Phase;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.enums.RegisterStage;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.cards.ProgrammableCard;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import dk.dtu.roborally.models.cards.UpgradeCard;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.PendingChoice;
import dk.dtu.roborally.models.game.Round;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Register;
import dk.dtu.roborally.models.player.Robot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Converts a live game to JSON and back. The board itself is reloaded from the
 * level file; this stores everything that changes during play.
 */
public final class GameStateCodec {

	private final Gson gson = new Gson();

	public String toJson(Game game) {
		StoredGame stored = new StoredGame();
		stored.status = game.getGameStatus().name();
		stored.phase = game.getCurrentRound().getCurrentPhase().name();
		stored.winnerPlayerId = game.getWinner() == null
				? null
				: game.getWinner().getId();
		stored.upgradeOrder = new ArrayList<>();
		for (Player player : game.getUpgradeOrder()) {
			stored.upgradeOrder.add(player.getId());
		}
		stored.shop = new StoredShop();
		stored.shop.deck = names(game.getUpgradeShop().getDeckTypes());
		stored.shop.discard = names(game.getUpgradeShop().getDiscardTypes());
		stored.shop.forSale = new ArrayList<>();
		for (UpgradeCard card : game.getUpgradeShop().getCards()) {
			stored.shop.forSale.add(card.getType().name());
		}
		Round round = game.getCurrentRound();
		stored.register = round.getCurrentRegisterIndex().orElse(null);
		stored.stage = round.getStage() == null
				? null
				: round.getStage().name();
		stored.activationOrder = round.getActivationOrder();
		stored.activated = round.getActivatedCount();
		stored.choice = storeChoice(round);
		stored.players = new ArrayList<>();
		for (Player player : game.getPlayersInGame()) {
			stored.players.add(storePlayer(player));
		}
		return gson.toJson(stored);
	}

	public Game toGame(String json, Board board, GameLobby lobby) {
		StoredGame stored = gson.fromJson(json, StoredGame.class);
		if (stored == null || stored.players == null || stored.shop == null) {
			throw new IllegalStateException("Stored game is empty");
		}

		Map<String, User> users = new HashMap<>();
		for (User user : lobby.getUsersInLobby()) {
			users.put(user.getId(), user);
		}

		Map<String, Player> playersById = new HashMap<>();
		for (StoredPlayer storedPlayer : stored.players) {
			Player player = loadPlayer(storedPlayer, users);
			playersById.put(player.getId(), player);
		}

		Game game = new Game(lobby.getGameId(), board,
				new HashSet<>(playersById.values()));
		game.setGameStatus(GameStatus.valueOf(stored.status));
		game.getCurrentRound().setCurrentPhase(Phase.valueOf(stored.phase));
		if (stored.winnerPlayerId != null) {
			game.setWinner(requirePlayer(playersById, stored.winnerPlayerId));
		}
		game.getCurrentRound().restoreActivation(stored.register,
				stored.stage == null
						? null
						: RegisterStage.valueOf(stored.stage),
				stored.activationOrder == null
						? List.of()
						: stored.activationOrder,
				stored.activated);
		if (stored.choice != null) {
			game.getCurrentRound()
					.restoreChoice(
							new PendingChoice(stored.choice.playerId,
									ChoiceType.valueOf(stored.choice.type),
									stored.choice.options),
							stored.choice.answer);
		}
		game.getUpgradeShop().restore(types(stored.shop.deck),
				types(stored.shop.discard), types(stored.shop.forSale));

		List<Player> order = new ArrayList<>();
		if (stored.upgradeOrder != null) {
			for (String playerId : stored.upgradeOrder) {
				order.add(requirePlayer(playersById, playerId));
			}
		}
		game.setUpgradeOrder(order);
		return game;
	}

	private static StoredChoice storeChoice(Round round) {
		PendingChoice choice = round.getPendingChoice().orElse(null);
		String answer = round.getAnswer().orElse(null);
		if (choice == null && answer == null) {
			return null;
		}
		if (choice == null) {
			// Answered but not used yet, the question is still on the round
			choice = round.getAnsweredChoice().orElseThrow();
		}
		StoredChoice stored = new StoredChoice();
		stored.playerId = choice.playerId();
		stored.type = choice.type().name();
		stored.options = choice.options();
		stored.answer = answer;
		return stored;
	}

	private static StoredPlayer storePlayer(Player player) {
		StoredPlayer stored = new StoredPlayer();
		stored.id = player.getId();
		stored.userId = player.getUser().getId();
		stored.color = player.getColor().name();
		stored.x = player.getRobot().getCurrentSpace().x();
		stored.y = player.getRobot().getCurrentSpace().y();
		stored.orientation = player.getRobot().getOrientation().name();
		stored.energy = player.getEnergy();
		stored.checkpointsReached = player.getCheckpointsReached();
		stored.submitted = player.getPlayerMat().isSubmitted();
		stored.drawPile = instructions(player.getPlayerMat().getDrawPile());
		stored.hand = instructions(player.getPlayerMat().getHand());
		stored.discard = instructions(player.getPlayerMat().getDiscardPile());
		stored.registers = new ArrayList<>();
		for (Register register : player.getPlayerMat().getRegisters()) {
			stored.registers.add(instructionOrNull(register.getCard()));
		}
		return stored;
	}

	private static Player loadPlayer(StoredPlayer stored,
			Map<String, User> users) {
		User user = users.get(stored.userId);
		if (user == null) {
			throw new IllegalStateException(
					"Player user is not in the lobby: " + stored.userId);
		}
		Robot robot = new Robot(new Vector2D(stored.x, stored.y),
				Direction.valueOf(stored.orientation));
		Player player = new Player(stored.id, user,
				PlayerColor.valueOf(stored.color), robot);
		player.restoreProgress(stored.energy, stored.checkpointsReached);
		player.getPlayerMat().restore(commands(stored.drawPile),
				commands(stored.hand), commands(stored.discard),
				commandsAllowingNull(stored.registers), stored.submitted);
		return player;
	}

	private static Player requirePlayer(Map<String, Player> players,
			String playerId) {
		Player player = players.get(playerId);
		if (player == null) {
			throw new IllegalStateException(
					"Stored player is missing: " + playerId);
		}
		return player;
	}

	private static List<String> instructions(List<ProgrammableCard> cards) {
		List<String> names = new ArrayList<>();
		for (ProgrammableCard card : cards) {
			names.add(instruction(card));
		}
		return names;
	}

	private static String instructionOrNull(ProgrammableCard card) {
		if (card == null) {
			return null;
		}
		return instruction(card);
	}

	private static String instruction(ProgrammableCard card) {
		if (card instanceof ProgrammingCard programmingCard) {
			return programmingCard.getInstruction().name();
		}
		throw new IllegalStateException("Only programming cards can be stored");
	}

	private static List<String> names(List<UpgradeType> types) {
		List<String> names = new ArrayList<>();
		for (UpgradeType type : types) {
			names.add(type.name());
		}
		return names;
	}

	private static List<UpgradeType> types(List<String> names) {
		List<UpgradeType> types = new ArrayList<>();
		if (names == null) {
			return types;
		}
		for (String name : names) {
			types.add(UpgradeType.valueOf(name));
		}
		return types;
	}

	private static List<CommandInstruction> commands(List<String> names) {
		List<CommandInstruction> commands = new ArrayList<>();
		if (names == null) {
			return commands;
		}
		for (String name : names) {
			commands.add(CommandInstruction.valueOf(name));
		}
		return commands;
	}

	private static List<CommandInstruction> commandsAllowingNull(
			List<String> names) {
		List<CommandInstruction> commands = new ArrayList<>();
		if (names == null) {
			return commands;
		}
		for (String name : names) {
			commands.add(
					name == null ? null : CommandInstruction.valueOf(name));
		}
		return commands;
	}

	private static final class StoredGame {
		private String status;
		private String phase;
		private String winnerPlayerId;
		private List<String> upgradeOrder;
		private Integer register;
		private String stage;
		private List<String> activationOrder;
		private int activated;
		private StoredChoice choice;
		private StoredShop shop;
		private List<StoredPlayer> players;
	}

	private static final class StoredChoice {
		private String playerId;
		private String type;
		private List<String> options;
		private String answer;
	}

	private static final class StoredShop {
		private List<String> deck;
		private List<String> discard;
		private List<String> forSale;
	}

	private static final class StoredPlayer {
		private String id;
		private String userId;
		private String color;
		private int x;
		private int y;
		private String orientation;
		private int energy;
		private int checkpointsReached;
		private boolean submitted;
		private List<String> drawPile;
		private List<String> hand;
		private List<String> discard;
		private List<String> registers;
	}
}
