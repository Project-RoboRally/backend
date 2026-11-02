package dk.dtu.roborally.models;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;
/**
 * Represents a user of the online game platform. A user's participation in a
 * specific game is represented by a Player.
 *
 * @author August, Elias
 */
@Getter
public class User {

	private final String id;
	private String username;
	private final String passwordHash;

	public User(String username) {
		this(UUID.randomUUID().toString(), username, null);
	}

	public User(String id, String username, String passwordHash) {
		this.id = Objects.requireNonNull(id);
		this.username = Objects.requireNonNull(username);
		this.passwordHash = passwordHash;
	}

}
