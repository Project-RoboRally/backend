package dk.dtu.roborally.engine.services;

import dk.dtu.roborally.api.dto.CreateLobbyDTO;
import dk.dtu.roborally.exceptions.lobby.LobbyCreatorNotFoundOnCreate;
import dk.dtu.roborally.exceptions.lobby.LobbyInvalidName;
import dk.dtu.roborally.exceptions.lobby.LobbyNotFoundException;
import dk.dtu.roborally.exceptions.lobby.LobbyUserAlreadyInLobby;
import dk.dtu.roborally.exceptions.lobby.NotAuthorizedException;
import dk.dtu.roborally.exceptions.lobby.UserNotInLobby;
import dk.dtu.roborally.exceptions.user.UserNotFound;
import dk.dtu.roborally.loaders.Config;
import dk.dtu.roborally.loaders.JsonLoader;
import dk.dtu.roborally.models.User;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.lobby.GameLobby;
import dk.dtu.roborally.repository.GameRepository;
import dk.dtu.roborally.repository.LobbyRepository;
import dk.dtu.roborally.repository.UserRepository;

import java.util.List;

public class LobbyService {
	private final LobbyRepository lobbyRepository;
	private final UserRepository userRepository;
	private final GameRepository gameRepository;
	private final Config config;

	public LobbyService(LobbyRepository lobbyRepository,
			UserRepository userRepository, GameRepository gameRepository) {
		this.lobbyRepository = lobbyRepository;
		this.userRepository = userRepository;
		this.gameRepository = gameRepository;
		this.config = JsonLoader.loadConfig();
	}

	public List<GameLobby> getLobbies() {
		return lobbyRepository.getAll();
	}

	public GameLobby createLobby(CreateLobbyDTO payload) {
		validateLobbyName(payload.name());

		User owner = userRepository.getByUsername(payload.username())
				.orElseThrow(() -> new LobbyCreatorNotFoundOnCreate(
						payload.username()));

		if (userInLobby(owner.getUsername()))
			throw new LobbyUserAlreadyInLobby(payload.username());

		GameLobby lobby = new GameLobby(config, payload.name(), owner);
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

		if (userInLobby(user.getUsername()))
			throw new LobbyUserAlreadyInLobby(username);

		GameLobby lobby = getLobby(id);
		lobby.addUserToLobby(user);

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
		}

		return lobby;
	}

	public GameLobby renameLobby(String id, String name) {
		GameLobby lobby = getLobby(id);

		validateLobbyName(name);
		lobby.setLobbyName(name);
		return lobby;
	}

	public void kickUser(String id, String kickedBy, String userKicked) {
		User user = userRepository.getByUsername(userKicked)
				.orElseThrow(UserNotFound::new);

		GameLobby lobby = getLobby(id);

		if (lobby.getLobbyOwner() == null
				|| !lobby.getLobbyOwner().getUsername().equals(kickedBy))
			throw new NotAuthorizedException(kickedBy);

		if (!userInLobby(lobby, user.getUsername()))
			throw new UserNotInLobby(userKicked);

		lobby.removeUser(user.getId());
	}

	public GameLobby setReady(String lobbyId, String username, boolean ready) {
		User user = userRepository.getByUsername(username)
				.orElseThrow(UserNotFound::new);

		GameLobby lobby = getLobby(lobbyId);

		if (!userInLobby(lobby, username))
			throw new UserNotInLobby(username);

		lobby.setReady(user.getId(), ready);

		return lobby;
	}

	public Game startGame(String lobbyId, String username) {
		GameLobby lobby = getLobby(lobbyId);

		if (lobby.getLobbyOwner() == null
				|| !lobby.getLobbyOwner().getUsername().equals(username))
			throw new NotAuthorizedException(username);

		Game game = lobby.startGame();
		gameRepository.add(game);

		return game;
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
		return lobbyRepository.getAll().stream()
				.anyMatch(lobby -> userInLobby(lobby, username));
	}

	public boolean userInLobby(GameLobby lobby, String username) {
		return lobby.getUsersInLobby().stream()
				.anyMatch(user -> user.getUsername().equals(username));
	}

	public GameLobby getUserLobby(String username) {
		return lobbyRepository.getAll().stream()
				.filter(lobby -> userInLobby(lobby, username)).findFirst()
				.orElse(null);
	}
}
