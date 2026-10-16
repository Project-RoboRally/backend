package dk.dtu.roborally.websocket.dto;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;

/**
 * Public information about a player that every player in the game may see.
 *
 * @author Elias
 */
public record PlayerDTO(String username, RobotDTO robot, int checkpointsReached,
		boolean programSubmitted) {

	public record RobotDTO(int x, int y, Direction orientation,
			boolean rebootedThisRound) {

		public static RobotDTO from(Robot robot) {
			return new RobotDTO(robot.getCurrentSpace().x(),
					robot.getCurrentSpace().y(), robot.getOrientation(),
					robot.isRebootedThisRound());
		}
	}

	public static PlayerDTO from(Player player) {
		return new PlayerDTO(player.getUser().getUsername(),
				RobotDTO.from(player.getRobot()),
				player.getCheckpointsReached(),
				player.getPlayerMat().isSubmitted());
	}
}
