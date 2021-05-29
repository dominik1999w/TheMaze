package physics;

import util.Point2D;

public class HitboxHistory<H extends Hitbox> {

    private final H hitbox;
    private final Point2D previousPosition = new Point2D();

    HitboxHistory(H hitbox) {
        this.hitbox = hitbox;
        this.previousPosition.set(hitbox.getPosition());
    }

    public H getHitbox() {
        return this.hitbox;
    }

    public Point2D getPreviousPosition() {
        return this.previousPosition;
    }

    public void setPreviousPosition(Point2D nextPosition) {
        this.previousPosition.set(previousPosition);
    }
}
