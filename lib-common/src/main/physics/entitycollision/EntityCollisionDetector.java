package physics.entitycollision;

import physics.HitboxHistory;

public interface EntityCollisionDetector {
    boolean detectCollision(HitboxHistory historyA, HitboxHistory historyB);
}