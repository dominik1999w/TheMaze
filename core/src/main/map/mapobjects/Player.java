package map.mapobjects;

import input.IPlayerInput;
import map.Map;
import map.config.MapConfig;
import util.Point2D;

public class Player {
    private Point2D position;
    private float rotation;
    private float speed;
    private final Map map;
    private final CollisionFinder collisionFinder;

    private Bullet bullet;

    public Player(Point2D position, Map map) {
        this.map = map;
        collisionFinder = new CollisionFinder(map, PlayerConfig.HITBOX_RADIUS);

        this.position = position.multiply(MapConfig.BOX_SIZE);
        this.rotation = 0;
        this.speed = PlayerConfig.INITIAL_SPEED;
    }

    public Point2D getPosition() {
        return position;
    }

    public float getRotation() {
        return rotation;
    }

    public Bullet getBullet() {
        return bullet;
    }

    public void updatePosition(IPlayerInput playerInput, float delta) {
        Point2D deltaPosition = new Point2D(
                playerInput.getX(),
                playerInput.getY()
        ).multiply(MapConfig.BOX_SIZE*speed*delta);

        position = collisionFinder.getNewPosition(position, deltaPosition);

        if (playerInput.getX() != 0 || playerInput.getY() != 0) {
            rotation = (float)Math.toDegrees(Math.atan2(playerInput.getY(), playerInput.getX()));
        }

        if(bullet != null) {
            bullet.updatePosition(delta);
        }
    }

    public void shoot() {
        if (bullet == null) {
            bullet = new Bullet(this,position,rotation,map);
        }
    }

    public void bulletImpact() {
        bullet = null;
    }
}
