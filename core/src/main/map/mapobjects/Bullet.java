package map.mapobjects;

import map.Map;
import map.config.MapConfig;
import util.Point2D;

public class Bullet {
    private final Player player;
    private Point2D position;
    private final float rotation;
    private final float speed;
    private final CollisionFinder collisionFinder;

    Bullet(Player player, Point2D position, float angle, Map map) {
        this.player = player;
        collisionFinder = new CollisionFinder(map, 0.075f);

        this.position = new Point2D(position).add(
                // shift position to fire out of the gun
                new Point2D(
                        (float)Math.cos(Math.toRadians(angle-30)),
                        (float)Math.sin(Math.toRadians(angle-30))
                ).multiply(MapConfig.BOX_SIZE*PlayerConfig.HITBOX_RADIUS)
        );

        this.rotation = angle;
        this.speed = PlayerConfig.INITIAL_SPEED * 2.5f;
    }

    public Point2D getPosition() {
        return position;
    }

    public float getRotation() {
        return rotation;
    }

    public void updatePosition(float delta) {
        Point2D deltaPosition = new Point2D(
                (float)Math.cos(Math.toRadians(rotation)),
                (float)Math.sin(Math.toRadians(rotation))
        ).multiply(MapConfig.BOX_SIZE*speed*delta);

        Point2D newPosition = collisionFinder.getNewPosition(position, deltaPosition);
        if(collisionFinder.found()) {
            player.bulletImpact();
        } else {
            position = newPosition;
        }
    }

}
