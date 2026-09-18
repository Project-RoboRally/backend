package dk.dtu.roborally.api.dto;

import java.util.List;


// TODO: update once Lobby model is updated with id and name, and we've talked about boards creation
public record LobbyDTO(String id, String name, List<String> players) {
}
