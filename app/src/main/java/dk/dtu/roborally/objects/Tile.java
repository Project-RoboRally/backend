package dk.dtu.roborally.objects;

import lombok.Getter;

import java.util.List;

public class Tile {
    private Vector2D position;

    @Getter
    private List<BoardElement> boardElementsList;

    public Vector2D getPosition() {
        return position;
    }

    public void setPosition(Vector2D new_position) {
        position.set(new_position);
    }

    public void setPosition(int x, int y) {
        position.set(x, y);
    }

    public void addBoardElement(BoardElement boardElement) {
        boardElementsList.add(boardElement);
    }

    public void removeBoardElement(BoardElement boardElement) {
        boardElementsList.remove(boardElement);
    }
}