package dk.dtu.roborally.engine.systems;

import dk.dtu.roborally.enums.CommandInstruction;
import dk.dtu.roborally.models.cards.ProgrammableCard;
import dk.dtu.roborally.models.cards.ProgrammingCard;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Register;
import dk.dtu.roborally.models.player.Robot;

/**
 * Handles programming and execution of player registers.
 *
 * @author Sebastian
 */
public class RegisterSystem {

	public boolean executeRegister(Player player, int registerIndex) {
		if (player == null || registerIndex < 0 || registerIndex >= player
				.getPlayerMat().getRegisters().length) {
			return false;
		}

		Robot robot = player.getRobot();
		Register register = player.getPlayerMat().getRegisters()[registerIndex];
		ProgrammableCard card = register.getCard();
		if (robot == null || !robot.isActive() || card == null
				|| !(card instanceof ProgrammingCard programmingCard)) {
			return false;
		}

		executeCommand(robot, programmingCard.getInstruction());
		return true;
	}

	private void executeCommand(Robot robot, CommandInstruction command) {
		switch (command) {
			case MOVE_1 :
				robot.moveForward(1);
				break;
			case MOVE_2 :
				robot.moveForward(2);
				break;
			case MOVE_3 :
				robot.moveForward(3);
				break;
			case LEFT_TURN :
				robot.turnLeft();
				break;
			case RIGHT_TURN :
				robot.turnRight();
				break;
			case U_TURN :
				robot.turnAround();
				break;
			case MOVE_BACK :
				robot.moveForward(-1);
				break;
			case AGAIN :
				break;
		}
	}
}
