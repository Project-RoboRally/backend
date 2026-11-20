package dk.dtu.roborally.exceptions.auth;
/**
 * @author Nicoleta
 */

public class LoginException extends RuntimeException {
	public LoginException(String message) {
		super(message);
	}
}
