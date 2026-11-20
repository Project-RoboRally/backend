package dk.dtu.roborally.api.dto;

/**
 * A player's answer to the choice activation is waiting on. The choice is one
 * of the options sent with the CHOICE_REQUIRED message.
 *
 * @author Sebastian
 */
public record MakeChoiceDTO(String choice) {
}
