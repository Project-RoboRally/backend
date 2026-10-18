package dk.dtu.roborally.models.player;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.PlayerColor;

/**
 * One robot's pose, copied so later movement does not change an earlier frame.
 */
public record RobotView(String username, PlayerColor color, int x, int y,
		Direction orientation) {

	public static RobotView from(Player player) {
		return new RobotView(player.getUser().getUsername(), player.getColor(),
				player.getRobot().getCurrentSpace().x(),
				player.getRobot().getCurrentSpace().y(),
				player.getRobot().getOrientation());
	}
}
