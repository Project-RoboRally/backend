package dk.dtu.roborally.websocket.dto;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.cards.ProgrammableCard;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import java.util.List;

/**
 * Public information about a player that every player in the game may see,
 * installed upgrades included. {@code program} is only filled during
 * activation, when everyone's registers are public.
 *
 * @author Elias, Nicoleta, Sebastian
 */
public record PlayerDTO(String username, RobotDTO robot, int checkpointsReached,
		int energy, boolean programSubmitted, List<UpgradeType> upgrades,
		List<CardDTO> program) {

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
		return from(player, false);
	}

	/**
	 * @param revealProgram
	 *            when true (activation), include all five register cards so
	 *            every client can show everyone's program
	 */
	public static PlayerDTO from(Player player, boolean revealProgram) {
		List<CardDTO> program = revealProgram
				? player.getPlayerMat().getRegisters().stream()
						.map(register -> cardOrNull(register.getCard()))
						.toList()
				: List.of();
		return new PlayerDTO(player.getUser().getUsername(),
				RobotDTO.from(player.getRobot()),
				player.getCheckpointsReached(), player.getEnergy(),
				player.getPlayerMat().isSubmitted(),
				player.getUpgrades().getAll(), program);
	}

	private static CardDTO cardOrNull(ProgrammableCard card) {
		return card == null ? null : CardDTO.from(card);
	}
}
