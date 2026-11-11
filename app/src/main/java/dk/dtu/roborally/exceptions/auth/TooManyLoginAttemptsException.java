package dk.dtu.roborally.exceptions.auth;

public class TooManyLoginAttemptsException extends RuntimeException {
	public TooManyLoginAttemptsException() {
		super("Too many failed login attempts, try again in a few minutes");
	}
}
