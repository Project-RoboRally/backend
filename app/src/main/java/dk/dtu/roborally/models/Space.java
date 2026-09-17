package dk.dtu.roborally.models;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single space on the Robo Rally board. A tile has a fixed position
 * and may contain multiple board elements.
 *
 * @author Matthias
 */
public class Space {

	@Getter
	private final Vector2D position;

	@Getter
	private final List<BoardElement> boardElements = new ArrayList<>();

	public Space(Vector2D position) {
		this.position = position;
	}

	public void addBoardElement(BoardElement boardElement) {
		boardElements.add(boardElement);
	}

	public void removeBoardElement(BoardElement boardElement) {
		boardElements.remove(boardElement);
	}
}
