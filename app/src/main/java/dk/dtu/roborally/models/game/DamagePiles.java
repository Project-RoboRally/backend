package dk.dtu.roborally.models.game;

import dk.dtu.roborally.enums.DamageInstruction;
import dk.dtu.roborally.models.cards.DamageCard;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The four damage piles next to the board. Every card in a pile is the same, so
 * a pile is just a count.
 *
 * @author Sebastian
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

	/**
	 * Takes a card from the given pile. The rulebook has the player pick
	 * another type when a pile runs out, we take from the biggest pile left.
	 * Empty only when every pile is empty.
	 */
	public Optional<DamageCard> take(DamageInstruction type) {
		DamageInstruction from = type;
		if (size(type) == 0) {
			from = piles.entrySet().stream()
					.max(Comparator.comparingInt(Map.Entry::getValue))
					.filter(pile -> pile.getValue() > 0).map(Map.Entry::getKey)
					.orElse(null);
			if (from == null) {
				return Optional.empty();
			}
		}
		piles.merge(from, -1, Integer::sum);
		return Optional.of(new DamageCard(from));
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
