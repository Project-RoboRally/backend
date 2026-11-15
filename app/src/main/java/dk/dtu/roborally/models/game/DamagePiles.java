package dk.dtu.roborally.models.game;

import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.models.cards.DamageCard;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The four damage piles next to the board. Every card in a pile is the same, so
 * a pile is just a count.
 *
 * @author Sebastian, Elias
 */
public class DamagePiles {

	private final Map<DamageInstruction, Integer> piles = new EnumMap<>(
			DamageInstruction.class);

	public DamagePiles() {
		for (DamageInstruction type : DamageInstruction.values()) {
			piles.put(type, type.getCopies());
		}
	}

	public int size(DamageInstruction type) {
		return piles.get(type);
	}

	/** Types that still have at least one card, in enum order. */
	public List<DamageInstruction> typesLeft() {
		return Arrays.stream(DamageInstruction.values())
				.filter(type -> size(type) > 0).toList();
	}

	/**
	 * Takes a card from the given pile. Empty when that pile has run out; the
	 * player then picks another type.
	 */
	public Optional<DamageCard> take(DamageInstruction type) {
		if (size(type) == 0) {
			return Optional.empty();
		}
		piles.merge(type, -1, Integer::sum);
		return Optional.of(new DamageCard(type));
	}

	/** Puts a played or discarded damage card back on its pile. */
	public void giveBack(DamageCard card) {
		piles.merge(Objects.requireNonNull(card).getInstruction(), 1,
				Integer::sum);
	}

	public Map<DamageInstruction, Integer> getSizes() {
		return Map.copyOf(piles);
	}

	/** Puts back pile sizes loaded from storage. */
	public void restore(Map<DamageInstruction, Integer> sizes) {
		for (DamageInstruction type : DamageInstruction.values()) {
			piles.put(type, sizes.getOrDefault(type, type.getCopies()));
		}
	}
}
