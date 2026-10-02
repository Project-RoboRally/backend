package dk.dtu.roborally.exceptions.lobby;

/**
 * Thrown when an action is not authorized for user
 *
 * @author Nicoleta
 */
public class NotAuthorizedException extends RuntimeException {
	public NotAuthorizedException(String username) {
		super("User " + username + " not authorized to do this action.");
	}
}
