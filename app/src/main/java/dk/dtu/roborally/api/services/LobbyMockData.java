package dk.dtu.roborally.api.services;

import dk.dtu.roborally.api.dto.LobbyDTO;

import java.util.List;

/**
 * TEMPORARY data taken from the frontend for testing.
 *
 * @author Nicoleta
 */
final class LobbyMockData {

	static final List<LobbyDTO> LOBBIES = List.of(
			new LobbyDTO("training-ground", "Training Ground", List.of("Ada", "Bjarne"), "Ada"),
			new LobbyDTO("factory-floor", "Factory Floor", List.of("Clara", "David", "Emil", "Fiona", "George"), "Clara"),
			new LobbyDTO("laser-maze", "Laser Maze", List.of("Fiona", "George"), "Fiona"),
			new LobbyDTO("crusher-canyon", "Crusher Canyon", List.of("Hanna", "Ivan", "Julia"), "Hanna"),
			new LobbyDTO("conveyor-chaos", "Conveyor Chaos", List.of("Kasper"), "Kasper"),
			new LobbyDTO("power-plant", "Power Plant", List.of("Lina", "Malik"), "Lina"),
			new LobbyDTO("warehouse-wars", "Warehouse Wars", List.of("Nora", "Oscar", "Pia"), "Nora"),
			new LobbyDTO("assembly-line", "Assembly Line", List.of("Quinn"), "Quinn")
	);

	private LobbyMockData() {
	}
}