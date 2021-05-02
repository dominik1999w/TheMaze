package entity.bullet;

import java.util.UUID;

import map.MapConfig;
import util.Point2D;

public class BulletController {

    private final Bullet bullet;

    public BulletController(Bullet bullet) {
        this.bullet = bullet;
    }

    public void update(float delta) {
        Point2D deltaPosition = new Point2D(
                (float)Math.cos(Math.toRadians(bullet.getRotation())),
                (float)Math.sin(Math.toRadians(bullet.getRotation()))
        ).multiply(MapConfig.BOX_SIZE*bullet.getSpeed()*delta);

        bullet.getPosition().add(deltaPosition);
    }

    public Bullet getBullet() {
        return bullet;
    }
}
