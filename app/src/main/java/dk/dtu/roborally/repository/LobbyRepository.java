package dk.dtu.roborally.repository;

import java.util.List;
import java.util.Optional;

import dk.dtu.roborally.models.lobby.GameLobby;

public interface LobbyRepository {
	boolean add(GameLobby lobby);

	boolean exists(String lobbyId);

	Optional<GameLobby> getById(String lobbyId);

	List<GameLobby> getAll();

	void delete(String lobbyId);
}
