package dk.dtu.roborally.api.services;

import dk.dtu.roborally.api.dto.CreateLobbyDTO;
import dk.dtu.roborally.api.dto.LobbyDTO;
import dk.dtu.roborally.exceptions.lobby.LobbyNotFoundException;
import dk.dtu.roborally.exceptions.lobby.NotAuthorizedException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory mock implementation of {@link ILobbyService}, seeded with mock data.
 * In-memory can be exchanged for a different stored data structure (json)
 * Should be changed to the real lobby service once implemented
 * All functionality from Lobby controller regarding data/logic was moved here
 * @author Nicoleta
 */
@Service
@Primary
public class LobbyServiceMock implements ILobbyService {
    private final Map<String, Lobby> lobbies = new LinkedHashMap<>();

    public LobbyServiceMock() {
        for (LobbyDTO mock : LobbyMockData.LOBBIES) {
            Lobby lobby = new Lobby(mock.id(), mock.name(), mock.createdBy());
            lobby.players.addAll(mock.players());
            lobbies.put(mock.id(), lobby);
        }
    }

    @Override
    public List<LobbyDTO> getLobbies() {
        List<LobbyDTO> result = new ArrayList<>();
        for (Lobby lobby : lobbies.values()) {
            result.add(lobby.toDTO());
        }
        return result;
    }

    @Override
    public LobbyDTO createLobby(CreateLobbyDTO payload) {
        String id = UUID.randomUUID().toString();
        Lobby lobby = new Lobby(id, payload.name(), payload.username());
        lobby.players.add(payload.username());
        lobbies.put(id, lobby);
        return lobby.toDTO();
    }

    @Override
    public LobbyDTO getLobby(String id) {
        return getLobbyOrThrow(id).toDTO();
    }

    @Override
    public LobbyDTO joinLobby(String id, String username) {
        Lobby lobby = getLobbyOrThrow(id);
        if (!lobby.players.contains(username)) {
            lobby.players.add(username);
        }
        return lobby.toDTO();
    }

    @Override
    public LobbyDTO leaveLobby(String id, String username) {
        Lobby lobby = getLobbyOrThrow(id);
        lobby.players.remove(username);
        return lobby.toDTO();
    }

    @Override
    public LobbyDTO renameLobby(String id, String name) {
        Lobby lobby = getLobbyOrThrow(id);
        lobby.name = name;
        return lobby.toDTO();
    }

    @Override
    public LobbyDTO kickPlayer(String id, String kickedBy, String playerKicked) {
        Lobby lobby = getLobbyOrThrow(id);
        if (!lobby.createdBy.equals(kickedBy)) {
            throw new NotAuthorizedException(kickedBy);
        }
        lobby.players.remove(playerKicked);
        return lobby.toDTO();
    }

    private Lobby getLobbyOrThrow(String id) {
        Lobby lobby = lobbies.get(id);
        if (lobby == null) {
            throw new LobbyNotFoundException(id);
        }
        return lobby;
    }

    // TODO: remove once Lobby object is built as needed
    private static class Lobby {
        private final String id;
        private String name;
        private final List<String> players = new ArrayList<>();
        private final String createdBy;

        private Lobby(String id, String name, String createdBy) {
            this.id = id;
            this.name = name;
            this.createdBy = createdBy;
        }

        private LobbyDTO toDTO() {
            return new LobbyDTO(id, name, List.copyOf(players), createdBy);
        }
    }
}
