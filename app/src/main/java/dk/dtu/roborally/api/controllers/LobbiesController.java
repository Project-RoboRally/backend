package dk.dtu.roborally.api.controllers;

import dk.dtu.roborally.api.dto.CreateLobbyDTO;
import dk.dtu.roborally.api.dto.JoinLobbyDTO;
import dk.dtu.roborally.api.dto.LobbyDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lobbies")
public class LobbiesController {
    private final Map<String, Lobby> lobbies = new LinkedHashMap<>();

    // TODO: hand over most of these below to a lobby service which reads data from DB/a more complete data file
    // only transformations to DTOs probably would remain here, and we would get the data from the the service
    public LobbiesController() {
        for (LobbyDTO mock : LobbyMockData.LOBBIES) {
            Lobby lobby = new Lobby(mock.id(), mock.name());
            lobby.players.addAll(mock.players());
            lobbies.put(mock.id(), lobby);
        }
    }

    // TODO: remove once Lobby object is built as needed
    private static class Lobby {
        private final String id;
        private final String name;
        private final List<String> players = new ArrayList<>();

        private Lobby(String id, String name) {
            this.id = id;
            this.name = name;
        }

        private LobbyDTO toDTO() {
            return new LobbyDTO(id, name, List.copyOf(players));
        }
    }

    // add functionality to request info every second or so, or decide in the group if needs to be websocket/broadcast info
    @GetMapping
    public ResponseEntity<List<LobbyDTO>> getLobbies() {
        List<LobbyDTO> result = new ArrayList<>();
        for (Lobby lobby : lobbies.values()) {
            result.add(lobby.toDTO());
        }
        System.out.println(result);
        return ResponseEntity.ok(result);
    }

    // TODO: move to separate LobbyController (on the main-menu no creation functionality)
    @PostMapping
    public ResponseEntity<LobbyDTO> createLobby(@RequestBody CreateLobbyDTO payload) {
        System.out.println("Current lobbies: " + lobbies.values().stream().map(Lobby::toDTO).toList());
        String id = payload.id();
        Lobby lobby = new Lobby(id, payload.name());
        lobby.players.add(payload.username());
        lobbies.put(id, lobby);
        System.out.println("New lobby to be added: " + lobby.name + " " + lobby.id);
        System.out.println("New lobbies list: " + lobbies.values().stream().map(Lobby::toDTO).toList());

        return ResponseEntity.ok(lobby.toDTO());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LobbyDTO> getLobby(@PathVariable String id) {
        return ResponseEntity.ok(getLobbyOrThrow(id).toDTO());
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<LobbyDTO> joinLobby(@PathVariable String id, @RequestBody JoinLobbyDTO payload) {
        Lobby lobby = getLobbyOrThrow(id);
        if (!lobby.players.contains(payload.username())) {
            lobby.players.add(payload.username());
        }
        return ResponseEntity.ok(lobby.toDTO());
    }

    private Lobby getLobbyOrThrow(String id) {
        Lobby lobby = lobbies.get(id);
        if (lobby == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found");
        }
        return lobby;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", ex.getReason()));
    }

}
