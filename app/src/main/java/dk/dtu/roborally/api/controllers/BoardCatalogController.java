package dk.dtu.roborally.api.controllers;

import dk.dtu.roborally.api.dto.BoardOptionDTO;
import dk.dtu.roborally.loaders.BoardCatalog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Lists the boards a lobby can be played on. The preview of the board a lobby
 * is on comes from GET /api/lobbies/{id}/board, which also covers uploaded
 * boards.
 *
 * @author August
 */
@RestController
@RequestMapping("/api/boards")
public class BoardCatalogController {

	private final BoardCatalog boards;

	public BoardCatalogController(BoardCatalog boards) {
		this.boards = boards;
	}

	@GetMapping
	public List<BoardOptionDTO> getBoards() {
		return boards.all().stream().map(BoardOptionDTO::from).toList();
	}
}
