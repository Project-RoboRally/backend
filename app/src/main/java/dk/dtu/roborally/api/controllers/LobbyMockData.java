package dk.dtu.roborally.api.controllers;

import dk.dtu.roborally.api.dto.LobbyDTO;

import java.util.List;

/**
 * Data taken from the frontend for testing.
 *
 * @author Nicoleta
 */

final class LobbyMockData {

	static final List<LobbyDTO> LOBBIES = List.of(
			new LobbyDTO("training-ground", "Training Ground", List.of("Ada", "Bjarne")),
			new LobbyDTO("factory-floor", "Factory Floor", List.of("Clara", "David", "Emil", "Fiona", "George")),
			new LobbyDTO("laser-maze", "Laser Maze", List.of("Fiona", "George")),
			new LobbyDTO("crusher-canyon", "Crusher Canyon", List.of("Hanna", "Ivan", "Julia")),
			new LobbyDTO("conveyor-chaos", "Conveyor Chaos", List.of("Kasper")),
			new LobbyDTO("power-plant", "Power Plant", List.of("Lina", "Malik")),
			new LobbyDTO("warehouse-wars", "Warehouse Wars", List.of("Nora", "Oscar", "Pia")),
			new LobbyDTO("assembly-line", "Assembly Line", List.of("Quinn"))
	);

	private LobbyMockData() {
	}
}