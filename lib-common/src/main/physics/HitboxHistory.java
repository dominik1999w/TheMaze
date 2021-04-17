package physics;

import util.Point2D;

class HitboxHistory {

    private final Hitbox hitbox;
    private final Point2D previousPosition;

    HitboxHistory(Hitbox hitbox, Point2D previousPosition) {
        this.hitbox = hitbox;
        this.previousPosition = previousPosition;
    }

    Hitbox getHitbox() {
        return this.hitbox;
    }

    Point2D getPreviousPosition() {
        return this.previousPosition;
    }

    void setPreviousPosition(Point2D updatedPosition) {
        previousPosition.set(updatedPosition);
    }
}
