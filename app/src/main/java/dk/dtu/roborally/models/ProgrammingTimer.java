package dk.dtu.roborally.models;

import java.time.Duration;

import lombok.Getter;
/**
 * Represents the round's programming time limit.
 * The first submitted program starts a shared 30-second countdown.
 * Later submissions do not restart it.
 * On expiry, game logic fills remaining empty registers
 * from the affected players' draw piles.
 *
 * @author August
 */
public class ProgrammingTimer {
    @Getter
    private static final Duration TIME_LIMIT = Duration.ofSeconds(30);

    // TODO: Implement starting the timer and checking expiry.
}
