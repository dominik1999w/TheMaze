package mapobjects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import input.IPlayerInput;
import map.config.MapConfig;
import map.generator.MapGenerator;
import renderable.PlayerView;

public class Player {
    private final PlayerView playerView;
    private Vector2 position;
    private float rotation;
    private float speed;
    private final MapGenerator mapGenerator;
    private final CollisionFinder collisionFinder;

    private Bullet bullet;

    public Player(Vector2 position, MapGenerator mapGenerator) {
        playerView = new PlayerView();
        this.mapGenerator = mapGenerator;
        collisionFinder = new CollisionFinder(mapGenerator, PlayerConfig.HITBOX_RADIUS);

        this.position = position;
        this.position.x *= MapConfig.BOX_SIZE;
        this.position.y *= MapConfig.BOX_SIZE;
        this.rotation = 0;
        this.speed = PlayerConfig.INITIAL_SPEED;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void updatePosition(IPlayerInput playerInput, float delta) {
        Vector2 deltaPosition = new Vector2();
        deltaPosition.x = playerInput.getX() * MapConfig.BOX_SIZE * speed * delta;
        deltaPosition.y = playerInput.getY() * MapConfig.BOX_SIZE * speed * delta;

        position = collisionFinder.getNewPosition(position, deltaPosition);

        if (playerInput.getX() != 0 || playerInput.getY() != 0) {
            rotation = (float) (Math.atan2(playerInput.getY(), playerInput.getX()) * (180 / Math.PI));
        }

        playerView.setPosition(position);
        playerView.setRotation(rotation);
        if(bullet != null) {
            bullet.updatePosition(delta);
        }
    }

    public void shoot() {
        if (bullet == null) {
            System.out.println("SHOOT!");
            bullet = new Bullet(this,position,rotation,mapGenerator);
        }
    }

    public void bulletImpact() {
        bullet = null;
    }

    public void render(SpriteBatch spriteBatch) {
        playerView.render(spriteBatch);
        if(bullet != null) {
            bullet.render(spriteBatch);
        }
    }
}
