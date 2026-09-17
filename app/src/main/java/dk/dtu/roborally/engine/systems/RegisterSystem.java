package dk.dtu.roborally.engine.systems;

import dk.dtu.roborally.enums.Command;
import dk.dtu.roborally.models.Player;
import dk.dtu.roborally.models.Register;
import dk.dtu.roborally.models.Robot;

/**
 * Handles programming and execution of player registers.
 *
 * @author Sebastian
 */
public class RegisterSystem {

    public boolean executeRegister(Player player, int registerIndex) {
        if (player == null || registerIndex < 0
                || registerIndex >= player.getPlayerMat().getRegisters().length) {
            return false;
        }

        Robot robot = player.getRobot();
        Register register = player.getPlayerMat().getRegisters()[registerIndex];
        if (robot == null || !robot.isActive() || register.getCommand() == null) {
            return false;
        }

        executeCommand(robot, register.getCommand());
        return true;
    }

    private void executeCommand(Robot robot, Command command) {
        switch (command) {
            case MOVE_1:
                robot.moveForward(1);
                break;
            case MOVE_2:
                robot.moveForward(2);
                break;
            case MOVE_3:
                robot.moveForward(3);
                break;
            case LEFT_TURN:
                robot.turnLeft();
                break;
            case RIGHT_TURN:
                robot.turnRight();
                break;
            case U_TURN:
                robot.turnAround();
                break;
            case POWER_UP:
                robot.setDamage(Math.max(0, robot.getDamage() - 1));
                break;
        }
    }
}
