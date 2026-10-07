package dk.dtu.roborally.exceptions.user;

public class UserNotFound extends RuntimeException {
	public UserNotFound() {
		super("user not found.");
	}
}
