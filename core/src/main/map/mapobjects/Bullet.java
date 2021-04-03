package map.mapobjects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import map.Map;
import map.config.MapConfig;
import renderable.BulletView;

public class Bullet {
    private final BulletView bulletView;
    private final map.mapobjects.Player player;
    private Vector2 position;
    private float rotation;
    private float speed;
    private final Map map;
    private map.mapobjects.CollisionFinder collisionFinder;

    Bullet(Player player, Vector2 position, float angle, Map map) {
        bulletView = new BulletView();
        this.player = player;
        this.map = map;
        collisionFinder = new CollisionFinder(map, 0.075f);

        this.position = new Vector2(position);
        // shift position to fire out of the gun
        this.position.x += (float)Math.cos((angle-30)/180*Math.PI) * MapConfig.BOX_SIZE * PlayerConfig.HITBOX_RADIUS;
        this.position.y += (float)Math.sin((angle-30)/180*Math.PI) * MapConfig.BOX_SIZE * PlayerConfig.HITBOX_RADIUS;

        this.rotation = angle;
        this.speed = PlayerConfig.INITIAL_SPEED * 2.5f;
    }

    public void updatePosition(float delta) {
        Vector2 deltaPosition = new Vector2();
        deltaPosition.x = (float)Math.cos(rotation/180*Math.PI) * MapConfig.BOX_SIZE * speed * delta;
        deltaPosition.y = (float)Math.sin(rotation/180*Math.PI) * MapConfig.BOX_SIZE * speed * delta;

        Vector2 newPosition = collisionFinder.getNewPosition(position, deltaPosition);
        if(collisionFinder.found()) {
            player.bulletImpact();
        } else {
            position = newPosition;
        }

        bulletView.setPosition(position);
        bulletView.setRotation(rotation);
    }

    public void render(SpriteBatch spriteBatch) {
        bulletView.render(spriteBatch);
    }

}
