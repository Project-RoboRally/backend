package dk.dtu.roborally.models.game;

import dk.dtu.roborally.enums.ChoiceType;
import java.util.List;
import java.util.Objects;

/**
 * A decision activation is waiting on. The options are the only answers the
 * player may give.
 *
 * @author Sebastian
 */
public record PendingChoice(String playerId, ChoiceType type,
		List<String> options) {

	public PendingChoice {
		Objects.requireNonNull(playerId);
		Objects.requireNonNull(type);
		options = List.copyOf(options);
	}
}
