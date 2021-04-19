package physics;

import util.Point2D;

public class HitboxHistory {

    private final Hitbox hitbox;
    private final Point2D previousPosition;

    HitboxHistory(Hitbox hitbox, Point2D previousPosition) {
        this.hitbox = hitbox;
        this.previousPosition = previousPosition;
    }

    public Hitbox getHitbox() {
        return this.hitbox;
    }

    public Point2D getPreviousPosition() {
        return this.previousPosition;
    }

    void setPreviousPosition(Point2D updatedPosition) {
        previousPosition.set(updatedPosition);
    }
}
