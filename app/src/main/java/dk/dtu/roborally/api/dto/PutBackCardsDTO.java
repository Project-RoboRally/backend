package dk.dtu.roborally.api.dto;

import java.util.List;

/**
 * Hand cards going back on top of the deck after a Memory Swap. The first card
 * named ends up on top.
  * @author Sebastian
 */
public record PutBackCardsDTO(List<String> cards) {
}
