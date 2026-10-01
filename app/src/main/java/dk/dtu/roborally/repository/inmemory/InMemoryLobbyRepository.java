package dk.dtu.roborally.repository.inmemory;

import dk.dtu.roborally.models.GameLobby;
import dk.dtu.roborally.repository.LobbyRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryLobbyRepository implements LobbyRepository {
	private final List<GameLobby> lobbies = new ArrayList<>();

	@Override
	public boolean add(GameLobby lobby) {
		if (exists(lobby.getLobbyId()))
			return false;

		lobbies.add(lobby);
		return true;
	}

	@Override
	public boolean exists(String lobbyId) {
		return lobbies.stream().anyMatch(l -> l.getLobbyId().equals(lobbyId));
	}

	@Override
	public Optional<GameLobby> getById(String lobbyId) {
		return lobbies.stream().filter(l -> l.getLobbyId().equals(lobbyId))
				.findFirst();
	}

	@Override
	public List<GameLobby> getAll() {
		return List.copyOf(lobbies);
	}

	@Override
	public void delete(String lobbyId) {
		lobbies.removeIf(l -> l.getLobbyId().equals(lobbyId));
	}
}
