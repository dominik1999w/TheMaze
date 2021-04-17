package entity.bullet;

import map.MapConfig;
import physics.mapcollision.MapCollisionFinder;
import util.Point2D;

public class BulletController {

    private final Bullet bullet;
    private final MapCollisionFinder collisionFinder;

    public BulletController(Bullet bullet, MapCollisionFinder collisionFinder) {
        this.bullet = bullet;
        this.collisionFinder = collisionFinder;
    }

    public Bullet getBullet() {
        return bullet;
    }

    public boolean update(float delta) {
        Point2D deltaPosition = new Point2D(
                (float)Math.cos(Math.toRadians(bullet.getRotation())),
                (float)Math.sin(Math.toRadians(bullet.getRotation()))
        ).multiply(MapConfig.BOX_SIZE*bullet.getSpeed()*delta);

        MapCollisionFinder.MapCollisionInfo collisionInfo = collisionFinder.getNewPosition(
                bullet.getPosition(), deltaPosition, BulletConfig.HITBOX_RADIUS);
        if (collisionInfo.hasCollided) {
            return false;
        } else {
            bullet.setPosition(collisionInfo.nextPosition);
            return true;
        }
    }
}
