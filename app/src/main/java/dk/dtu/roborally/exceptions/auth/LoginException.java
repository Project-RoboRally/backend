package dk.dtu.roborally.exceptions.auth;


public class LoginException extends RuntimeException {
    public LoginException(String message) {
        super(message);
    }
}
