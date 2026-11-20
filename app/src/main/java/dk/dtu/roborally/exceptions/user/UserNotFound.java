package dk.dtu.roborally.exceptions.user;
/**
 * @author August
 */

public class UserNotFound extends RuntimeException {
	public UserNotFound() {
		super("user not found.");
	}
}
