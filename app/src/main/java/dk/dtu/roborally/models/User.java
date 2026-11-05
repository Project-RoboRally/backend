package dk.dtu.roborally.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.util.UUID;

/**
 * Represents a user of the online game platform. A user's participation in a
 * specific game is represented by a Player.
 *
 * @author August, Anton
 */
@Getter
public class User {

	private final String id;
	private String username;
	@JsonIgnore
	private String passwordHash;

	public User(String username) {
		this(username, null);
	}

	public User(String username, String passwordHash) {
		this.id = UUID.randomUUID().toString();
		this.username = username;
		this.passwordHash = passwordHash;
	}

}
