package dk.dtu.roborally.api.dto;

import java.util.List;

/**
 * Five register cards submitted by one player, named like MOVE_1 or SPAM.
 */
public record ProgramDTO(List<String> registers) {
}
