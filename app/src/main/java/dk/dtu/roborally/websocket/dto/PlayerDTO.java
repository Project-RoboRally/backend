package dk.dtu.roborally.websocket.dto;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;

/**
 * Public information about a player that every player in the game may see.
 *
 * @author Elias, Nicoleta, Sebastian
 */
public record PlayerDTO(String username, RobotDTO robot, int checkpointsReached,
		int energy, boolean programSubmitted) {

	public record RobotDTO(int x, int y, Direction orientation,
			boolean rebootedThisRound) {

		public static RobotDTO from(Robot robot) {
			// Convert the robot to the corresponding DTO
			return new RobotDTO(robot.getCurrentSpace().x(),
					robot.getCurrentSpace().y(), robot.getOrientation(),
					robot.isRebootedThisRound());
		}
	}

	public static PlayerDTO from(Player player) {
		// Convert the player to the corresponding DTO
		return new PlayerDTO(player.getUser().getUsername(),
				RobotDTO.from(player.getRobot()),
				player.getCheckpointsReached(), player.getEnergy(),
				player.getPlayerMat().isSubmitted());
	}
}
