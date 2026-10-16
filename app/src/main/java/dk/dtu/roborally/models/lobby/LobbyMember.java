package dk.dtu.roborally.models.lobby;

import java.util.Objects;

import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.models.User;

public record LobbyMember(User user, PlayerColor color, boolean ready) {

	public LobbyMember {
		Objects.requireNonNull(user);
		Objects.requireNonNull(color);
	}

	public LobbyMember(User user, PlayerColor color) {
		this(user, color, false);
	}

	public LobbyMember withReady(boolean ready) {
		return new LobbyMember(user, color, ready);
	}
}
