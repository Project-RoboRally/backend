package dk.dtu.roborally.api.services;

import dk.dtu.roborally.api.dto.CreateLobbyDTO;
import dk.dtu.roborally.api.dto.LobbyDTO;

import java.util.List;

/**
 * Handles lobby creation, retrieval, and player joining/leaving/being kicked out.
 *
 * @author Nicoleta
 */
public interface ILobbyService {

    List<LobbyDTO> getLobbies();

    LobbyDTO createLobby(CreateLobbyDTO payload);

    LobbyDTO getLobby(String id);

    LobbyDTO joinLobby(String id, String username);

    LobbyDTO leaveLobby(String id, String username);

    LobbyDTO renameLobby(String id, String name);

    LobbyDTO kickPlayer(String id, String kickedBy, String playerKicked);

}
