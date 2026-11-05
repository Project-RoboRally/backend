package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.board.Vector2D;
import dk.dtu.roborally.models.board.elements.RebootPoint;
import dk.dtu.roborally.models.board.elements.StartingPoint;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.player.Player;
import dk.dtu.roborally.models.player.Register;
import java.util.Optional;

/**
 * Brings back robots that fell off the board or into a pit, or played a worm.
 *
 * @author Sebastian
 */
public final class Reboot {

	private static final int DAMAGE = 2;

	private Reboot() {
	}

	/**
	 * Reboots the robot like the rulebook says: take 2 SPAM (unless it was the
	 * Reboot upgrade), the rest of the program is cancelled, and the robot goes
	 * on the reboot token covering the space it fell from. Off the start board
	 * it goes back to its start space instead. A robot already standing there
	 * gets pushed off in the token's direction. The player picks a facing
	 * afterwards.
	 */
	public static void reboot(Game game, Player player, boolean takeDamage,
			Vector2D fellFrom) {
		if (takeDamage) {
			Damage.take(game, player, DamageInstruction.SPAM, DAMAGE);
		}
		cancelProgram(game, player);

		Optional<RebootPoint> token = game.getBoard()
				.getElements(RebootPoint.class).stream()
				.filter(point -> point.covers(fellFrom)).findFirst();
		Vector2D target = token.map(RebootPoint::getPosition)
				.orElse(player.getRobot().getStartSpace());
		Direction push = token.map(RebootPoint::getDirection)
				.orElseGet(() -> startFacing(game, target));

		Optional<Player> onToken = game.getPlayersInGame().stream()
				.filter(other -> other != player
						&& target.equals(other.getRobot().getCurrentSpace()))
				.findFirst();
		// Out of the way first, so a long push can not run into it
		player.getRobot().reboot(target);
		onToken.ifPresent(other -> Movement.step(game, other, push));
		player.getRobot().face(push);
		if (game.getCurrentRound().isActivating()) {
			game.getCurrentRound().addRebootToFace(player.getId());
		}
	}

	/**
	 * The registers from the current one on are discarded, cards and all, so
	 * the robot does nothing more this round.
	 */
	private static void cancelProgram(Game game, Player player) {
		int from = game.getCurrentRound().getCurrentRegisterIndex().orElse(0);
		for (Register register : player.getPlayerMat().getRegisters()) {
			if (register.getIndex() >= from && register.getCard() != null) {
				player.getPlayerMat().addToDiscardPile(register.getCard());
				register.setCard(null);
			}
		}
	}

	private static Direction startFacing(Game game, Vector2D start) {
		return game.getBoard().getElements(StartingPoint.class).stream()
				.filter(point -> point.getPosition().equals(start))
				.map(StartingPoint::getOrientation).findFirst()
				.orElse(Direction.NORTH);
	}
}
