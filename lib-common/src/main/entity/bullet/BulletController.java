package entity.bullet;

import map.CollisionFinder;
import map.Map;
import map.config.MapConfig;
import util.Point2D;

public class BulletController {

    private final Bullet bullet;
    private final CollisionFinder collisionFinder;

    public BulletController(Bullet bullet, Map map) {
        this.bullet = bullet;
        this.collisionFinder = new CollisionFinder(map, BulletConfig.HITBOX_RADIUS);
    }

    public Bullet getBullet() {
        return bullet;
    }

    public boolean update(float delta) {
        Point2D deltaPosition = new Point2D(
                (float)Math.cos(Math.toRadians(bullet.getRotation())),
                (float)Math.sin(Math.toRadians(bullet.getRotation()))
        ).multiply(MapConfig.BOX_SIZE*bullet.getSpeed()*delta);

        Point2D newPosition = collisionFinder.getNewPosition(bullet.getPosition(), deltaPosition);
        if(collisionFinder.found()) {
            return false;
        } else {
            bullet.setPosition(newPosition);
            return true;
        }
    }
}
