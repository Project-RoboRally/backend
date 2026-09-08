package dk.dtu.roborally.objects;

import lombok.Getter;

public class Board {
    @Getter
    private Vector2D dimentions;

    @Getter
    private Tile[][] tiles;

}
