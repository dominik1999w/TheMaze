package entity.bullet;

import map.MapConfig;
import util.Point2D;

public class BulletController {

    private final Bullet bullet;

    private final Point2D velocity;

    public BulletController(Bullet bullet) {
        this.bullet = bullet;
        this.velocity = new Point2D(
                (float) Math.cos(Math.toRadians(bullet.getRotation())),
                (float) Math.sin(Math.toRadians(bullet.getRotation()))
        ).multiply(MapConfig.BOX_SIZE * bullet.getSpeed());
    }

    public void update(float delta) {
        Point2D deltaPosition = getVelocity().multiply(delta);
        bullet.getPosition().add(deltaPosition);
    }

    public Bullet getBullet() {
        return bullet;
    }

    public Point2D getVelocity() {
        return new Point2D(velocity);
    }
}
