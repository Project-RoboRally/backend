package dk.dtu.roborally.models.lobby;

import java.util.Objects;

import dk.dtu.roborally.enums.PlayerColor;
import dk.dtu.roborally.models.User;

public record LobbyMember(User user, PlayerColor color) {

    public LobbyMember {
        Objects.requireNonNull(user);
        Objects.requireNonNull(color);
    }
}
