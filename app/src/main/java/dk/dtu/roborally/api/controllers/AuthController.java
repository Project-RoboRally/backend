package dk.dtu.roborally.api.controllers;

import dk.dtu.roborally.api.dto.LoginDTO;
import dk.dtu.roborally.api.dto.LoginResponseDTO;
import dk.dtu.roborally.exceptions.auth.LoginException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AuthController {

    @PostMapping("/api/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginDTO payload) {
        String username = payload.username();

        return ResponseEntity.ok(new LoginResponseDTO(username));
    }

    @ExceptionHandler(LoginException.class)
    public ResponseEntity<Map<String, String>> handleLoginException(LoginException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleMissingBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error",
                "Request body is required. Send JSON with Content-Type application/json, e.g. {\"username\":\"alice\",\"password\":\"password\"}."
        ));
    }
}