package debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.UUID;

import entity.bullet.BulletController;
import entity.bullet.BulletHitbox;
import entity.player.Player;
import entity.player.PlayerHitbox;
import entity.player.PlayerInput;
import entity.player.controller.AuthoritativePlayerController;
import entity.player.controller.InputPlayerController;
import map.Map;
import map.MapConfig;
import map.generator.MapGenerator;
import physics.CollisionWorld;
import util.Point2D;
import world.World;

public class DebugScreen extends ScreenAdapter {
    private final DebugGameApp debugGameApp;
    private final SpriteBatch batch;
    private final AssetManager assetManager;

    private final OrthographicCamera camera;
    private final World<?> world;
    private final DebugWorldView worldView;
    private final CollisionWorld collisionWorld;

    private final InputPlayerController shooter;

    public DebugScreen(DebugGameApp debugGameApp, SpriteBatch batch, AssetManager assetManager) {
        this.debugGameApp = debugGameApp;
        this.batch = batch;
        this.assetManager = assetManager;

        this.camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        int mapWidth = 10; // temporary: number of boxes horizontal-wise
        camera.zoom = mapWidth * MapConfig.BOX_SIZE / (float) Gdx.graphics.getWidth();
        camera.position.set(2.5f * MapConfig.BOX_SIZE, 2.5f * MapConfig.BOX_SIZE, 0);
        camera.update();

        MapGenerator mapGenerator = new MapGenerator(5);
        Map map = mapGenerator.generateMap(0);
        this.world = new World<>(AuthoritativePlayerController::new, BulletController::new);

        this.shooter = new InputPlayerController(
                new Player(new Point2D(4.5f, 4f).multiply(MapConfig.BOX_SIZE), 275.0f),
                world);
        System.out.println("SHOOTER IS " + shooter.getPlayer().getId());
        this.collisionWorld = new CollisionWorld(map);
        collisionWorld.addPlayerHitbox(new PlayerHitbox(shooter.getPlayer(), world));

        this.worldView = new DebugWorldView(world, map, camera, shooter.getPlayer(), assetManager);

        world.subscribeOnPlayerAdded(newPlayer -> collisionWorld.addPlayerHitbox(new PlayerHitbox(newPlayer, world)));
        world.subscribeOnPlayerRemoved(collisionWorld::removePlayerHitbox);
        world.subscribeOnBulletAdded((shooterID, newBullet) -> collisionWorld.setBulletHitbox(new BulletHitbox(shooterID, newBullet, world)));
        world.subscribeOnBulletRemoved(collisionWorld::removeBulletHitbox);

        UUID target = UUID.randomUUID();
        System.out.println("TARGET IS " + target);
        world.getPlayerController(target, new Point2D(4.5f, 1f).multiply(MapConfig.BOX_SIZE));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.5f, .5f, .5f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.graphics.getFrameId() % 60 == 0) {
            shooter.updateInput(new PlayerInput(0, 0, true));
        }

        shooter.update();
        world.update(delta);
        collisionWorld.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        worldView.render(batch);
        batch.end();
    }
}
