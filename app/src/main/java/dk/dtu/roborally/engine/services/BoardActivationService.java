package dk.dtu.roborally.engine.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.board.Board;
import dk.dtu.roborally.models.board.Space;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.BoardElement;
import dk.dtu.roborally.models.board.elements.ConveyorBelt;
import dk.dtu.roborally.models.board.elements.EnergySpace;
import dk.dtu.roborally.models.board.elements.Gear;
import dk.dtu.roborally.models.board.elements.LaserEmitter;
import dk.dtu.roborally.models.board.elements.PushPanel;
import dk.dtu.roborally.models.board.elements.Wall;
import dk.dtu.roborally.models.cards.DamageCard;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Robot;
import dk.dtu.roborally.models.player.RobotView;

/**
 * Activates board elements for a register after all players have executed their
 * programming cards.
 * 
 * @author Victor
 */
public class BoardActivationService {

	public List<List<RobotView>> activateRegister(int register,
			List<Player> players, Board board) {
		List<List<RobotView>> steps = new ArrayList<>();
		Map<Vector2D, Space> spaces = board.getSpaceMap();

		for (Player player : players) {
			activateConveyor(player, players, board, spaces, true, steps);
		}
		for (Player player : players) {
			activateConveyor(player, players, board, spaces, false, steps);
		}
		for (Player player : players) {
			activatePushPanel(player, players, board, spaces, steps);
		}
		for (Player player : players) {
			activateGear(player, players, spaces, steps);
		}
		for (Player player : players) {
			activateBoardLasers(player, spaces);
		}
		for (Player player : players) {
			activateRobotLaser(player, players, board, spaces);
		}
		for (Player player : players) {
			activateEnergySpace(player, register, spaces);
		}

		return steps;
	}

	private void activateConveyor(Player player, List<Player> players,
			Board board, Map<Vector2D, Space> spaces, boolean blue,
			List<List<RobotView>> steps) {
		Space space = spaces.get(player.getRobot().getCurrentSpace());
		if (space == null) {
			return;
		}
		for (BoardElement element : space.getBoardElements()) {
			if (!(element instanceof ConveyorBelt belt)
					|| isBlue(belt) != blue) {
				continue;
			}

			int moveCount = blue ? 2 : 1;
			boolean moved = false;
			for (int i = 0; i < moveCount; i++) {
				if (!moveRobot(player, belt.getDirection(), players, board)) {
					break;
				}
				moved = true;
			}
			if (moved) {
				rotateForConveyor(player.getRobot(), belt);
				steps.add(snapshot(players));
			}
			return;
		}
	}

	private boolean isBlue(ConveyorBelt belt) {
		return switch (belt.getType()) {
			case GREEN_STRAIGHT, GREEN_RIGHT_CURVE, GREEN_LEFT_CURVE -> true;
			case BLUE_STRAIGHT, BLUE_RIGHT_CURVE, BLUE_LEFT_CURVE -> false;
		};
	}

	private void rotateForConveyor(Robot robot, ConveyorBelt belt) {
		switch (belt.getType()) {
			case BLUE_RIGHT_CURVE, GREEN_RIGHT_CURVE -> robot.turnRight();
			case BLUE_LEFT_CURVE, GREEN_LEFT_CURVE -> robot.turnLeft();
			case BLUE_STRAIGHT, GREEN_STRAIGHT -> {
			}
		}
	}

	private void activatePushPanel(Player player, List<Player> players,
			Board board, Map<Vector2D, Space> spaces,
			List<List<RobotView>> steps) {
		Space space = spaces.get(player.getRobot().getCurrentSpace());
		if (space == null) {
			return;
		}
		for (BoardElement element : space.getBoardElements()) {
			if (element instanceof PushPanel panel && moveRobot(player,
					panel.getPushDirection(), players, board)) {
				steps.add(snapshot(players));
				return;
			}
		}
	}

	private void activateGear(Player player, List<Player> players,
			Map<Vector2D, Space> spaces, List<List<RobotView>> steps) {
		Space space = spaces.get(player.getRobot().getCurrentSpace());
		if (space == null) {
			return;
		}
		for (BoardElement element : space.getBoardElements()) {
			if (element instanceof Gear gear) {
				switch (gear.getRotationDirection()) {
					case CLOCKWISE -> player.getRobot().turnRight();
					case COUNTERCLOCKWISE -> player.getRobot().turnLeft();
				}
				steps.add(snapshot(players));
			}
		}
	}

	private void activateBoardLasers(Player target,
			Map<Vector2D, Space> spaces) {
		for (Space space : spaces.values()) {
			for (BoardElement element : space.getBoardElements()) {
				if (element instanceof LaserEmitter laser && laserHitsPosition(
						laser.getPosition(), laser.getDirection(),
						target.getRobot().getCurrentSpace(), spaces)) {
					addDamage(target, laser.getDamage());
				}
			}
		}
	}

	private void activateRobotLaser(Player shooter, List<Player> players,
			Board board, Map<Vector2D, Space> spaces) {
		Vector2D position = shooter.getRobot().getCurrentSpace();
		Direction direction = shooter.getRobot().getOrientation();
		while (true) {
			Vector2D next = position.add(direction.step());
			if (!board.isWithinBoard(next) || wallBlocks(spaces.get(position),
					spaces.get(next), direction)
					|| hasPushPanel(spaces.get(next))) {
				return;
			}
			for (Player target : players) {
				if (target != shooter
						&& next.equals(target.getRobot().getCurrentSpace())) {
					addDamage(target, 1);
					return;
				}
			}
			position = next;
		}
	}

	private boolean laserHitsPosition(Vector2D source, Direction direction,
			Vector2D target, Map<Vector2D, Space> spaces) {
		Vector2D position = source;
		while (true) {
			Vector2D next = position.add(direction.step());
			Space currentSpace = spaces.get(position);
			Space nextSpace = spaces.get(next);
			if (nextSpace == null
					|| wallBlocks(currentSpace, nextSpace, direction)
					|| hasPushPanel(nextSpace)) {
				return false;
			}
			if (next.equals(target)) {
				return true;
			}
			position = next;
		}
	}

	private void activateEnergySpace(Player player, int register,
			Map<Vector2D, Space> spaces) {
		Space space = spaces.get(player.getRobot().getCurrentSpace());
		if (space == null) {
			return;
		}
		for (BoardElement element : space.getBoardElements()) {
			if (element instanceof EnergySpace energySpace) {
				if (register == 4) {
					player.addEnergy(1);
				}
				if (energySpace.getCubeAmount() > 0) {
					player.addEnergy(1);
					energySpace.addCubeAmount(-1);
				}
			}
		}
	}

	private boolean moveRobot(Player moving, Direction direction,
			List<Player> players, Board board) {
		List<Player> chain = new ArrayList<>();
		chain.add(moving);
		Vector2D position = moving.getRobot().getCurrentSpace();
		Player occupying;
		while (true) {
			Vector2D next = position.add(direction.step());
			if (!board.isWithinBoard(next)
					|| wallBlocks(board.getSpace(position.x(), position.y()),
							board.getSpace(next.x(), next.y()), direction)) {
				return false;
			}
			occupying = playerAt(players, next, moving);
			if (occupying == null) {
				break;
			}
			chain.add(occupying);
			position = next;
		}
		for (int i = chain.size() - 1; i >= 0; i--) {
			Robot robot = chain.get(i).getRobot();
			robot.moveTo(robot.getCurrentSpace().add(direction.step()));
		}
		return true;
	}

	private Player playerAt(List<Player> players, Vector2D position,
			Player moving) {
		for (Player player : players) {
			if (player != moving
					&& position.equals(player.getRobot().getCurrentSpace())) {
				return player;
			}
		}
		return null;
	}

	private boolean wallBlocks(Space from, Space to, Direction direction) {
		return hasWall(from, direction) || hasWall(to, direction.uTurn());
	}

	private boolean hasWall(Space space, Direction side) {
		if (space == null) {
			return false;
		}
		for (BoardElement element : space.getBoardElements()) {
			if (element instanceof Wall wall && wall.getSide() == side) {
				return true;
			}
		}
		return false;
	}

	private boolean hasPushPanel(Space space) {
		for (BoardElement element : space.getBoardElements()) {
			if (element instanceof PushPanel) {
				return true;
			}
		}
		return false;
	}

	private void addDamage(Player player, int amount) {
		for (int i = 0; i < amount; i++) {
			player.getPlayerMat()
					.addToDiscardPile(new DamageCard(DamageInstruction.SPAM));
		}
	}

	private List<RobotView> snapshot(List<Player> players) {
		return players.stream().map(RobotView::from).toList();
	}
}
