package dk.dtu.roborally.api.controllers;

import dk.dtu.roborally.api.dto.LobbyDTO;
import dk.dtu.roborally.api.dto.LoginDTO;
import dk.dtu.roborally.api.dto.LoginResponseDTO;
import dk.dtu.roborally.auth.LoginException;
import dk.dtu.roborally.lobby.LobbyException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AuthController {

    @PostMapping("/api/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginDTO payload, HttpServletResponse response) {
        var username = payload.username();
        var password = payload.password();

        // TODO: replace with actual login functionality
        if (!password.equals("password")) {
            throw new LoginException("Wrong username or password");
        }

        return ResponseEntity.ok(new LoginResponseDTO(username));
    }

    @ExceptionHandler(LoginException.class)
    public ResponseEntity<Map<String, String>> handleLoginException(LoginException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
    }
}