package dk.dtu.roborally.models.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

import dk.dtu.roborally.models.board.elements.BoardElement;
import dk.dtu.roborally.models.board.elements.StartingPoint;

import lombok.Getter;

/**
 * Represents the Robo Rally game board and its spaces.
 *
 * @author Elias, Matthias, Victor
 */
public class Board {
	@Getter
	Map<Vector2D, Space> spaceMap = new HashMap<>();

	public Board(Map<Vector2D, Space> tileMap) {
		this.spaceMap = tileMap;
	}

	public Space getSpace(int x, int y) {
		return spaceMap.get(new Vector2D(x, y));
	}

	public boolean isWithinBoard(Vector2D position) {
		return spaceMap.containsKey(position);
	}

	// sorted so callers get stable order
	public List<StartingPoint> getStartingPoints() {
		
		List<StartingPoint> startingPoints = new ArrayList<>();

		// Find all starting points
		for (Space space : spaceMap.values()) {
			for (BoardElement element : space.getBoardElements()) {
				if (element instanceof StartingPoint point) {
					startingPoints.add(point);
				}
			}
		}
		// Sort by y, then x (Makes startingpoint list deterministic)
		startingPoints.sort(Comparator
				.comparingInt((StartingPoint point) -> point.getPosition().y())
				.thenComparingInt(point -> point.getPosition().x()));

		return Collections.unmodifiableList(startingPoints);
	}
}
