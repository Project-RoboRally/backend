package dk.dtu.roborally.models;

import lombok.Getter;
import java.util.UUID;
/**
 * Represents a user of the online game platform. A user's participation in a
 * specific game is represented by a Player.
 *
 * @author August
 */
@Getter
public class User {

	private final String id;
	private final String username;

	public User(String username) {
		this.id = UUID.randomUUID().toString();
		this.username = username;
	}

}
