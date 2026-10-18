package dk.dtu.roborally.api.dto;

import dk.dtu.roborally.enums.CommandInstruction;

import java.util.List;

/**
 * Five register instructions submitted by one player.
 */
public record ProgramDTO(String username, List<CommandInstruction> registers) {
}
