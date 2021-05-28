package physics;

import util.Point2D;

public class HitboxHistory {

    private final Hitbox hitbox;
    private final Point2D previousPosition = new Point2D();

    HitboxHistory(Hitbox hitbox) {
        this.hitbox = hitbox;
        this.previousPosition.set(hitbox.getPosition());
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
