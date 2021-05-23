package entity.bullet;

import java.util.UUID;

import map.MapConfig;
import util.Point2D;

public class BulletController {

    private final UUID playerID;
    private final Bullet bullet;

    public BulletController(UUID playerID, Bullet bullet) {
        this.playerID = playerID;
        this.bullet = bullet;
    }

    public void update(float delta) {
        Point2D deltaPosition = new Point2D(
                (float)Math.cos(Math.toRadians(bullet.getRotation())),
                (float)Math.sin(Math.toRadians(bullet.getRotation()))
        ).multiply(MapConfig.BOX_SIZE*bullet.getSpeed()*delta);

        bullet.getPosition().add(deltaPosition);
    }

    public UUID getPlayerID() {
        return playerID;
    }

    public Bullet getBullet() {
        return bullet;
    }
}
