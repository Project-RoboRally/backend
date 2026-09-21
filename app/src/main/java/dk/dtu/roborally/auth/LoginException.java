package dk.dtu.roborally.auth;

/**
 * Represents an exception thrown when login fails.
 *
 * @author Nicoleta
 */
public class LoginException extends RuntimeException {
	public LoginException(String message) {
		super(message);
	}
}
