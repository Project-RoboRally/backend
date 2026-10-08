package dk.dtu.roborally.api.controllers;

import dk.dtu.roborally.api.dto.GameDTO;
import dk.dtu.roborally.engine.services.GameService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling API calls relating Game.
 *
 * @author Matthias
 */

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameDTO> getGame(@PathVariable String id) {
        return ResponseEntity.ok(
                GameDTO.from(gameService.getGame(id))
        );
    }
}