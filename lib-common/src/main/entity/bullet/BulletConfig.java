package entity.bullet;

import entity.player.PlayerConfig;
import map.MapConfig;
import util.Point2D;

public class BulletConfig {
    public static final float INITIAL_SPEED = PlayerConfig.INITIAL_SPEED * 2.5f;
    public static final float HITBOX_RADIUS = 0.075f; // tile size normalized to 1x1

    public static Point2D textureDependentShift(float angle) {
        return new Point2D(
                (float)Math.cos(Math.toRadians(angle-30)),
                (float)Math.sin(Math.toRadians(angle-30))
        ).multiply(MapConfig.BOX_SIZE*PlayerConfig.HITBOX_RADIUS);
    }

}
