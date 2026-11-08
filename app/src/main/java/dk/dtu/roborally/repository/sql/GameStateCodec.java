package dk.dtu.roborally.repository.sql;

import com.google.gson.Gson;

import dk.dtu.roborally.enums.ChoiceType;
import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.enums.Phase;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.enums.RegisterStage;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.EnergySpace;
import dk.dtu.roborally.models.cards.ProgrammableCard;
import dk.dtu.roborally.models.cards.UpgradeCard;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.PendingChoice;
import dk.dtu.roborally.models.game.Round;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Register;
import dk.dtu.roborally.models.player.Robot;

import java.time.Instant;
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
		stored.rebootsToFace = round.getRebootsToFace();
		stored.programmingEndsAt = round.getTimer().getEndsAt()
				.map(Instant::toEpochMilli).orElse(null);
		stored.energyCubes = new HashMap<>();
		for (EnergySpace space : game.getBoard()
				.getElements(EnergySpace.class)) {
			stored.energyCubes.put(spaceKey(space.getPosition()),
					space.getCubeAmount());
		}
		stored.damagePiles = new HashMap<>();
		game.getDamagePiles().getSizes().forEach(
				(type, size) -> stored.damagePiles.put(type.name(), size));
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
		if (stored.programmingEndsAt != null) {
			game.getCurrentRound().getTimer()
					.restore(Instant.ofEpochMilli(stored.programmingEndsAt));
		}
		if (stored.rebootsToFace != null) {
			game.getCurrentRound().restoreRebootsToFace(stored.rebootsToFace);
		}
		if (stored.choice != null) {
			game.getCurrentRound()
					.restoreChoice(
							new PendingChoice(stored.choice.playerId,
									ChoiceType.valueOf(stored.choice.type),
									stored.choice.options),
							stored.choice.answer);
		}
		if (stored.energyCubes != null) {
			for (EnergySpace space : board.getElements(EnergySpace.class)) {
				Integer cubes = stored.energyCubes
						.get(spaceKey(space.getPosition()));
				if (cubes != null) {
					space.setCubeAmount(cubes);
				}
			}
		}
		if (stored.damagePiles != null) {
			Map<DamageInstruction, Integer> sizes = new HashMap<>();
			stored.damagePiles.forEach((type, size) -> sizes
					.put(DamageInstruction.valueOf(type), size));
			game.getDamagePiles().restore(sizes);
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

	/** Json keys have to be strings, so a space is stored as "x,y". */
	private static String spaceKey(Vector2D position) {
		return position.x() + "," + position.y();
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
		stored.rebooted = player.getRobot().isRebootedThisRound();
		stored.startX = player.getRobot().getStartSpace().x();
		stored.startY = player.getRobot().getStartSpace().y();
		stored.energy = player.getEnergy();
		stored.checkpointsReached = player.getCheckpointsReached();
		stored.submitted = player.getPlayerMat().isSubmitted();
		stored.drawPile = instructions(player.getPlayerMat().getDrawPile());
		stored.hand = instructions(player.getPlayerMat().getHand());
		stored.discard = instructions(player.getPlayerMat().getDiscardPile());
		stored.registers = new ArrayList<>();
		stored.damagedRegisters = new ArrayList<>();
		for (Register register : player.getPlayerMat().getRegisters()) {
			stored.registers.add(instructionOrNull(register.getCard()));
			if (register.isDamaged()) {
				stored.damagedRegisters.add(register.getIndex());
			}
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
		Vector2D space = new Vector2D(stored.x, stored.y);
		Vector2D start = stored.startX == null
				? space
				: new Vector2D(stored.startX, stored.startY);
		Robot robot = new Robot(space, Direction.valueOf(stored.orientation),
				start);
		robot.restoreReboot(stored.rebooted);
		Player player = new Player(stored.id, user,
				PlayerColor.valueOf(stored.color), robot);
		player.restoreProgress(stored.energy, stored.checkpointsReached);
		player.getPlayerMat().restore(cards(stored.drawPile),
				cards(stored.hand), cards(stored.discard),
				cards(stored.registers), stored.submitted);
		if (stored.damagedRegisters != null) {
			for (int index : stored.damagedRegisters) {
				player.getPlayerMat().getRegisters().get(index)
						.setDamaged(true);
			}
		}
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
			names.add(card.getName());
		}
		return names;
	}

	private static String instructionOrNull(ProgrammableCard card) {
		return card == null ? null : card.getName();
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

	/** Cards by name, a null name is an empty register. */
	private static List<ProgrammableCard> cards(List<String> names) {
		List<ProgrammableCard> cards = new ArrayList<>();
		if (names == null) {
			return cards;
		}
		for (String name : names) {
			cards.add(name == null ? null : ProgrammableCard.named(name));
		}
		return cards;
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
		private List<String> rebootsToFace;
		private Long programmingEndsAt;
		private Map<String, Integer> damagePiles;
		private Map<String, Integer> energyCubes;
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
		private boolean rebooted;
		private Integer startX;
		private Integer startY;
		private int energy;
		private int checkpointsReached;
		private boolean submitted;
		private List<String> drawPile;
		private List<String> hand;
		private List<String> discard;
		private List<String> registers;
		private List<Integer> damagedRegisters;
	}
}
