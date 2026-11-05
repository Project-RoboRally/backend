package dk.dtu.roborally.models.board.elements;

import dk.dtu.roborally.enums.Direction;
import dk.dtu.roborally.models.board.Vector2D;
import lombok.Getter;
import java.util.Objects;

/**
 * A reboot token. Robots that fall off the part of the board this token covers
 * come back here. The direction is where a robot already on the token gets
 * pushed when another one reboots.
 *
 * @author Sebastian
 */
@Getter
public class RebootPoint extends BoardElement {

	private final Vector2D position;
	private final Direction direction;
	private final Vector2D areaStart;
	private final Vector2D areaEnd;

	/** The area is the rectangle between the two corners, both included. */
	public RebootPoint(Vector2D position, Direction direction,
			Vector2D areaStart, Vector2D areaEnd) {
		this.position = Objects.requireNonNull(position);
		this.direction = Objects.requireNonNull(direction);
		this.areaStart = Objects.requireNonNull(areaStart);
		this.areaEnd = Objects.requireNonNull(areaEnd);
	}

	/** Whether a robot falling from this space reboots on this token. */
	public boolean covers(Vector2D space) {
		return space.x() >= Math.min(areaStart.x(), areaEnd.x())
				&& space.x() <= Math.max(areaStart.x(), areaEnd.x())
				&& space.y() >= Math.min(areaStart.y(), areaEnd.y())
				&& space.y() <= Math.max(areaStart.y(), areaEnd.y());
	}
}
