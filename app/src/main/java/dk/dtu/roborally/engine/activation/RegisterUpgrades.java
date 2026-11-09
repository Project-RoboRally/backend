package dk.dtu.roborally.engine.activation;

import dk.dtu.roborally.enums.ChoiceType;
import dk.dtu.roborally.enums.UpgradeType;
import dk.dtu.roborally.models.game.Game;
import dk.dtu.roborally.models.game.PendingChoice;
import dk.dtu.roborally.models.game.Round;
import dk.dtu.roborally.models.player.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Upgrades used at the start of a register, before anyone plays a card. Admin
 * Privilege (once a round) and Manual Sort give priority for the register,
 * Manual Sort going before Admin Privilege. Deflector Shield costs 1 energy and
 * stops robot lasers hitting you this register.
 *
 * @author Sebastian
 */
public final class RegisterUpgrades {

	public static final String NONE = "NONE";

	private RegisterUpgrades() {
	}

	/**
	 * Asks every player with one of these upgrades, in priority order, whether
	 * to use it. Returns false while someone still has to answer. Once everyone
	 * has, the activation order for the register is set.
	 */
	static boolean ask(Game game) {
		Round round = game.getCurrentRound();
		List<Player> normalOrder = Priority.order(game);
		for (Player player : normalOrder) {
			while (!round.isStartDone(player.getId())) {
				List<UpgradeType> usable = usable(player, round);
				if (usable.isEmpty()) {
					round.markStartDone(player.getId());
					break;
				}
				List<String> options = new ArrayList<>();
				options.add(NONE);
				usable.forEach(type -> options.add(type.name()));
				Optional<String> answer = round.ask(new PendingChoice(
						player.getId(), ChoiceType.REGISTER_UPGRADE, options));
				if (answer.isEmpty()) {
					return false;
				}
				if (answer.get().equals(NONE)) {
					round.markStartDone(player.getId());
				} else {
					use(player, UpgradeType.valueOf(answer.get()), round);
				}
			}
		}
		round.setActivationOrder(order(normalOrder, round));
		return true;
	}

	private static List<UpgradeType> usable(Player player, Round round) {
		List<UpgradeType> usable = new ArrayList<>();
		if (player.getRobot().isRebootedThisRound()) {
			// Rebooted robots can not use upgrades
			return usable;
		}
		List<UpgradeType> used = round.getUsedAtRegisterStart(player.getId());
		for (UpgradeType type : List.of(UpgradeType.MANUAL_SORT,
				UpgradeType.ADMIN_PRIVILEGE, UpgradeType.DEFLECTOR_SHIELD)) {
			if (!player.getUpgrades().has(type) || used.contains(type)) {
				continue;
			}
			if (type == UpgradeType.ADMIN_PRIVILEGE
					&& player.getUpgrades().usedThisRound(type)) {
				continue;
			}
			if (type == UpgradeType.DEFLECTOR_SHIELD
					&& player.getEnergy() < 1) {
				continue;
			}
			usable.add(type);
		}
		return usable;
	}

	private static void use(Player player, UpgradeType type, Round round) {
		switch (type) {
			case MANUAL_SORT -> player.getUpgrades().remove(type);
			case ADMIN_PRIVILEGE -> player.getUpgrades().markUsed(type);
			case DEFLECTOR_SHIELD -> player.spendEnergy(1);
			default -> throw new IllegalArgumentException(
					type + " is not used at the start of a register");
		}
		round.useAtRegisterStart(player.getId(), type);
	}

	/** Manual Sort first, then Admin Privilege, then everyone else. */
	private static List<String> order(List<Player> normalOrder, Round round) {
		List<String> order = new ArrayList<>();
		for (UpgradeType jump : List.of(UpgradeType.MANUAL_SORT,
				UpgradeType.ADMIN_PRIVILEGE)) {
			for (Player player : normalOrder) {
				if (!order.contains(player.getId())
						&& round.getUsedAtRegisterStart(player.getId())
								.contains(jump)) {
					order.add(player.getId());
				}
			}
		}
		for (Player player : normalOrder) {
			if (!order.contains(player.getId())) {
				order.add(player.getId());
			}
		}
		return order;
	}
}
