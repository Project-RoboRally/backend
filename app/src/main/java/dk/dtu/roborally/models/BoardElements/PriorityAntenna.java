package dk.dtu.roborally.models.BoardElements;

/**
 * Represents the priority antenna of a game, 
 * which is a board element that occupies a space, 
 * meaning no other elements or robots can be on the same space.
 * The priority antenna calculates, which players registers should be activated first at each register.
 * This priority order is calculated by scanning clockwise starting at 12 o'clock 
 * and making a queue based on the robots distance to the priority antenna.
 *
 * @author August
 */
public class PriorityAntenna extends BoardElement {
    
}
