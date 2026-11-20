package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.api.dto.CreateLobbyDTO;
import dk.dtu.roborally.enums.GameStatus;
import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.exceptions.lobby.LobbyCreatorNotFoundOnCreate;
import dk.dtu.roborally.exceptions.lobby.LobbyInvalidName;
import dk.dtu.roborally.exceptions.lobby.LobbyNotFoundException;
import dk.dtu.roborally.exceptions.lobby.NotAuthorizedException;
import dk.dtu.roborally.exceptions.lobby.UserNotInLobby;
import dk.dtu.roborally.exceptions.user.UserNotFound;
import dk.dtu.roborally.loaders.BoardCatalog;
import dk.dtu.roborally.loaders.BoardInfo;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.repository.LobbyRepository;
import dk.dtu.roborally.repository.UserRepository;

import java.util.List;
/**
 * @author Elias, August, Matthias
 */

public class LobbyService {
	/** Characters in the largest board file an owner can upload. */
	static final int MAX_UPLOAD_LENGTH = 100_000;

	private final LobbyRepository lobbyRepository;
	private final UserRepository userRepository;
	private final GameService gameService;
	private final BoardCatalog boards;

	/** Uses the boards listed in config.json. */
	public LobbyService(LobbyRepository lobbyRepository,
			UserRepository userRepository, GameService gameService) {
		this(lobbyRepository, userRepository, gameService, BoardCatalog.load());
	}

	public LobbyService(LobbyRepository lobbyRepository,
			UserRepository userRepository, GameService gameService,
			BoardCatalog boards) {
		this.lobbyRepository = lobbyRepository;
		this.userRepository = userRepository;
		this.gameService = gameService;
		this.boards = boards;
	}

	public List<GameLobby> getLobbies() {
		return lobbyRepository.getAll().stream()
				.filter(lobby -> !lobby.isGameStarted() || gameService
						.getStatus(lobby.getGameId()) != GameStatus.FINISHED)
				.toList();
	}

	public GameLobby createLobby(CreateLobbyDTO payload) {
		validateLobbyName(payload.name());

		User owner = userRepository.getByUsername(payload.username())
				.orElseThrow(() -> new LobbyCreatorNotFoundOnCreate(
						payload.username()));

		GameLobby lobby = new GameLobby(boards.defaultBoard(), payload.name(),
				owner);
		lobbyRepository.add(lobby);

		return lobby;
	}

	public GameLobby getLobby(String id) {
		return lobbyRepository.getById(id)
				.orElseThrow(() -> new LobbyNotFoundException(id));
	}

	public GameLobby joinLobby(String id, String username) {
		User user = userRepository.getByUsername(username)
				.orElseThrow(UserNotFound::new);

		GameLobby lobby = getLobby(id);

		if (userInLobby(lobby, username)) {
			return lobby;
		}

		if (lobby.isGameStarted()) {
			throw new IllegalStateException("This game has already started.");
		}

		lobby.addUserToLobby(user);
		lobbyRepository.update(lobby);

		return lobby;
	}

	public GameLobby leaveLobby(String id, String username) {
		User user = userRepository.getByUsername(username)
				.orElseThrow(UserNotFound::new);

		GameLobby lobby = getLobby(id);

		if (!userInLobby(lobby, username))
			throw new UserNotInLobby(username);

		if (lobby.getLobbyOwner() != null
				&& lobby.getLobbyOwner().getUsername().equals(username)) {
			lobby.getUsersInLobby().stream()
					.filter(member -> !member.getUsername().equals(username))
					.toList()
					.forEach(member -> lobby.removeUser(member.getId()));

			lobby.removeUser(user.getId());
			lobbyRepository.delete(lobby.getLobbyId());
		} else {
			lobby.removeUser(user.getId());
			lobbyRepository.update(lobby);
		}

		return lobby;
	}

	public GameLobby renameLobby(String id, String name) {
		GameLobby lobby = getLobby(id);

		validateLobbyName(name);
		lobby.setLobbyName(name);
		lobbyRepository.update(lobby);
		return lobby;
	}

	public GameLobby startGame(String id, String username) {

		GameLobby lobby = getLobby(id);

		validateIsLobbyOwner(lobby, username);

		Game game = lobby.startGame();
		gameService.startGame(game);
		lobbyRepository.update(lobby);
		return lobby;
	}

	public GameLobby changeBoard(String id, String username, String boardId) {
		GameLobby lobby = getLobby(id);

		validateIsLobbyOwner(lobby, username);

		BoardInfo board = boards.byId(boardId)
				.orElseThrow(() -> new IllegalArgumentException(
						"Unknown board: " + boardId));

		lobby.changeBoard(board);
		lobbyRepository.update(lobby);
		return lobby;
	}

	/**
	 * Changes the board held by the lobby to the uploaded JSON file by the
	 * lobby owner
	 */
	public GameLobby uploadBoard(String id, String username, String levelJson) {
		GameLobby lobby = getLobby(id);

		validateIsLobbyOwner(lobby, username);

		if (levelJson == null || levelJson.isBlank())
			throw new IllegalArgumentException("The board file is empty.");

		// refuse if more than 1000 KB
		if (levelJson.length() > MAX_UPLOAD_LENGTH)
			throw new IllegalArgumentException(
					"The board file is too large (at most "
							+ MAX_UPLOAD_LENGTH / 1000 + " KB).");

		lobby.changeBoard(BoardInfo.uploaded(levelJson));
		lobbyRepository.update(lobby);
		return lobby;
	}

	public void kickUser(String id, String kickedBy, String toKickUsername) {
		User userToKick = userRepository.getByUsername(toKickUsername)
				.orElseThrow(UserNotFound::new);

		GameLobby lobby = getLobby(id);

		validateIsLobbyOwner(lobby, kickedBy);

		if (!userInLobby(lobby, userToKick.getUsername()))
			throw new UserNotInLobby(toKickUsername);

		lobby.kickUser(userToKick.getId());

		lobby.removeUser(userToKick.getId());
		lobbyRepository.update(lobby);
	}

	public void unKickUser(String id, String lobbyOwnerUsername,
			String kickedUsername) {

		User userToUnKick = userRepository.getByUsername(kickedUsername)
				.orElseThrow(UserNotFound::new);

		GameLobby lobby = getLobby(id);

		validateIsLobbyOwner(lobby, lobbyOwnerUsername);

		lobby.unKickUser(userToUnKick.getId());
		lobbyRepository.update(lobby);

	}

	public GameLobby setReady(String lobbyId, String username, boolean ready) {
		User user = userRepository.getByUsername(username)
				.orElseThrow(UserNotFound::new);

		GameLobby lobby = getLobby(lobbyId);

		if (!userInLobby(lobby, username))
			throw new UserNotInLobby(username);

		lobby.setReady(user.getId(), ready);
		lobbyRepository.update(lobby);

		return lobby;
	}

	public GameLobby setColor(String lobbyId, String username,
			PlayerColor color) {
		User user = userRepository.getByUsername(username)
				.orElseThrow(UserNotFound::new);

		GameLobby lobby = getLobby(lobbyId);

		if (!userInLobby(lobby, username))
			throw new UserNotInLobby(username);

		lobby.changeColor(user.getId(), color);
		lobbyRepository.update(lobby);

		return lobby;
	}

	private void validateIsLobbyOwner(GameLobby lobby, String ownerUsername) {
		if (lobby.getLobbyOwner().getUsername().equals(ownerUsername)) {
		} else {
			throw new NotAuthorizedException(ownerUsername);
		}
	}

	private void validateLobbyName(String name) {
		if (name == null || name.isBlank())
			throw new LobbyInvalidName("Name must not be blank");

		if (name.length() < 3)
			throw new LobbyInvalidName("Must be longer than 2 characters");

		if (name.length() > 16)
			throw new LobbyInvalidName("Must be shorter than 17 characters");
	}

	public boolean userInLobby(String username) {
		return getLobbies().stream()
				.anyMatch(lobby -> userInLobby(lobby, username));
	}

	public boolean userInLobby(GameLobby lobby, String username) {
		return lobby.getUsersInLobby().stream()
				.anyMatch(user -> user.getUsername().equals(username));
	}

	public GameLobby getUserLobby(String username) {
		return getLobbies().stream()
				.filter(lobby -> userInLobby(lobby, username)).findFirst()
				.orElse(null);
	}
}
