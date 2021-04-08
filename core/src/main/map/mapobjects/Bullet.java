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

        this.position = new Point2D(position);
        // shift position to fire out of the gun
        this.position.x += (float)Math.cos((angle-30)/180*Math.PI) * MapConfig.BOX_SIZE * PlayerConfig.HITBOX_RADIUS;
        this.position.y += (float)Math.sin((angle-30)/180*Math.PI) * MapConfig.BOX_SIZE * PlayerConfig.HITBOX_RADIUS;

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
        Point2D deltaPosition = new Point2D();
        deltaPosition.x = (float)Math.cos(rotation/180*Math.PI) * MapConfig.BOX_SIZE * speed * delta;
        deltaPosition.y = (float)Math.sin(rotation/180*Math.PI) * MapConfig.BOX_SIZE * speed * delta;

        Point2D newPosition = collisionFinder.getNewPosition(position, deltaPosition);
        if(collisionFinder.found()) {
            player.bulletImpact();
        } else {
            position = newPosition;
        }
    }

}
