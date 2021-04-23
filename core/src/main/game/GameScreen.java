package game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import connection.GameClient;
import entity.bullet.BulletController;
import entity.bullet.BulletHitbox;
import entity.player.PlayerHitbox;
import entity.player.controller.AuthoritativePlayerController;
import physics.CollisionWorld;
import world.World;
import renderable.WorldView;
import entity.player.Player;
import entity.player.controller.InputPlayerController;
import map.Map;
import map.MapConfig;
import map.generator.MapGenerator;
import ui.GameUI;
import util.Point2D;

public class GameScreen extends ScreenAdapter {

    private final OrthographicCamera camera;
    private final SpriteBatch batch;

    private final Player player;
    private final InputPlayerController playerController;
    private final CollisionWorld collisionWorld;

    private final GameUI gameUI;
    private final World<AuthoritativePlayerController> world;
    private final WorldView worldView;

    private final GameClient client;
    private int frameCounter = 0;

    private final DebugDrawer debugDrawer;

    public GameScreen(SpriteBatch batch, GameClient client, AssetManager assetManager) {
        this.batch = batch;

        this.client = client;
        int seed = this.client.connect();

        MapGenerator mapGenerator = new MapGenerator(50);
        Map map = mapGenerator.generateMap(seed);

        this.camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        this.gameUI = new GameUI(assetManager);

        this.world = new World<>(AuthoritativePlayerController::new, BulletController::new);
        this.player = new Player(new Point2D(3.5f * MapConfig.BOX_SIZE, 2.5f * MapConfig.BOX_SIZE));
        this.playerController = new InputPlayerController(player, gameUI.getPlayerInput(), world);
        this.collisionWorld = new CollisionWorld(map);
        world.subscribeOnPlayerAdded(newPlayer -> collisionWorld.addHitbox(new PlayerHitbox(newPlayer)));
        world.subscribeOnBulletAdded((player, newBullet) -> collisionWorld.addHitbox(new BulletHitbox(newBullet, world)));
        world.subscribeOnBulletRemoved(collisionWorld::removeHitbox);
        collisionWorld.addHitbox(new PlayerHitbox(player));

        this.worldView = new WorldView(world, map, camera, player, assetManager);

        int mapWidth = 10; // temporary: number of boxes horizontal-wise
        camera.zoom = mapWidth * MapConfig.BOX_SIZE / (float) Gdx.graphics.getWidth();
        camera.update();

        gameUI.build();

        client.enterGame(player, world);

        this.debugDrawer = new DebugDrawer(camera, map, player);
    }

    @Override
    public void render(float delta) {
        // read player input
        gameUI.readInput();

        // update the world according to player input
        playerController.update(delta);
        world.update(delta);
        collisionWorld.update();

        Point2D playerPosition = player.getPosition();
        camera.position.set(playerPosition.x(), playerPosition.y(), 0);
        camera.update();

        // render the world
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        worldView.render(batch);
        batch.end();

        //debugDrawer.draw();

        gameUI.render(delta);

        if (frameCounter % 5 == 0) client.syncGameState();
        frameCounter++;
    }

    @Override
    public void resize(int width, int height) {
        gameUI.resize(width, height);
    }

    @Override
    public void dispose() {
        gameUI.dispose();
    }
}
