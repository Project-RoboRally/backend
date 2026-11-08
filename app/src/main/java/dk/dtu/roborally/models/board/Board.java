package dk.dtu.roborally.models.board;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import dk.dtu.roborally.models.board.elements.BoardElement;
import dk.dtu.roborally.models.board.elements.StartingPoint;
import lombok.Getter;

/**
 * Represents the Robo Rally game board and its spaces.
 *
 * @author Elias, Matthias, Victor, Sebastian
 */
public class Board {
	@Getter
	private final Map<Vector2D, Space> spaceMap;

	public Board(Map<Vector2D, Space> spaceMap) {
		this.spaceMap = spaceMap;
	}

	public Space getSpace(int x, int y) {
		return getSpace(new Vector2D(x, y));
	}

	public Space getSpace(Vector2D position) {
		return spaceMap.get(position);
	}

	public boolean isWithinBoard(Vector2D position) {
		return spaceMap.containsKey(position);
	}

	/**
	 * Every element of the given type on the board. An element covering several
	 * spaces, like a conveyor belt, is only listed once.
	 */
	public <T extends BoardElement> List<T> getElements(Class<T> type) {
		return spaceMap.values().stream()
				.flatMap(space -> space.getBoardElements().stream())
				.filter(type::isInstance).map(type::cast).distinct().toList();
	}

	/** Sorted by y, then x, so callers get the same order every time. */
	public List<StartingPoint> getStartingPoints() {
		return getElements(StartingPoint.class).stream()
				.sorted(Comparator.comparingInt(
						(StartingPoint point) -> point.getPosition().y())
						.thenComparingInt(point -> point.getPosition().x()))
				.toList();
	}
}
