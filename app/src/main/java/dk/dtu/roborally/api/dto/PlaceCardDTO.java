package dk.dtu.roborally.api.dto;

/**
 * A hand card to put in a register while programming. register counts from 0.
 */
public record PlaceCardDTO(int register, String card) {
}
