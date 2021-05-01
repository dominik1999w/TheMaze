package game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import connection.GameClient;
import connection.PlayerInputLog;
import connection.ServerResponseListener;
import entity.bullet.BulletController;
import entity.bullet.BulletHitbox;
import entity.player.Player;
import entity.player.PlayerHitbox;
import entity.player.PlayerInput;
import entity.player.controller.AuthoritativePlayerController;
import entity.player.controller.LocalPlayerController;
import map.Map;
import map.MapConfig;
import physics.CollisionWorld;
import renderable.WorldView;
import ui.GameUI;
import util.Point2D;
import world.World;

public class GameScreen extends ScreenAdapter implements ServerResponseListener {

    private final OrthographicCamera camera;
    private final SpriteBatch batch;

    private final Player player;
    private final LocalPlayerController playerController;
    private final CollisionWorld collisionWorld;

    private final GameUI gameUI;
    private final World<AuthoritativePlayerController> world;
    private final WorldView worldView;

    private final GameClient client;
    private final PlayerInputLog playerInputLog;

    private final DebugDrawer debugDrawer;

    private final Lock lock = new ReentrantLock();

    public GameScreen(SpriteBatch batch, GameClient client, Map map, AssetManager assetManager) {
        this.batch = batch;

        this.playerInputLog = new PlayerInputLog();
        this.client = client;
        this.client.connect();

        this.camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        this.gameUI = new GameUI(assetManager);

        this.world = new World<>(AuthoritativePlayerController::new, BulletController::new);
        this.player = new Player(new Point2D(3.5f * MapConfig.BOX_SIZE, 2.5f * MapConfig.BOX_SIZE));
        this.playerController = new LocalPlayerController(player, world);
        this.collisionWorld = new CollisionWorld(map);
        // Uncomment this for CLIENT-SIDE PLAYER-MAP COLLISION HANDLING
        //world.subscribeOnPlayerAdded(newPlayer -> collisionWorld.addHitbox(new PlayerHitbox(newPlayer)));
        world.subscribeOnBulletAdded((player, newBullet) -> collisionWorld.addHitbox(new BulletHitbox(newBullet, world)));
        world.subscribeOnBulletRemoved(collisionWorld::removeHitbox);
        collisionWorld.addHitbox(new PlayerHitbox(player));

        this.worldView = new WorldView(world, map, camera, player, assetManager);

        int mapWidth = 10; // temporary: number of boxes horizontal-wise
        camera.zoom = mapWidth * MapConfig.BOX_SIZE / (float) Gdx.graphics.getWidth();
        camera.update();

        gameUI.build();

        client.enterGame(player.getId(), this);

        this.debugDrawer = new DebugDrawer(camera, map, player);
    }

    private final Queue<PlayerInput> inputQueue = new ArrayDeque<>();

    @Override
    public void render(float delta) {
        // read player input
        PlayerInput playerInput = gameUI.readInput();
        playerInput.setDelta(delta);

        lock.lock();
        inputQueue.add(playerInput);

        // update the world according to player input
        while (!inputQueue.isEmpty()) {
            playerController.notifyInput(inputQueue.poll());
            playerController.update();
            collisionWorld.update();
        }
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

        if (client.syncState(playerInputLog.getCurrentSequenceNumber(), playerInput)) {
            playerInputLog.log(playerInput);
        }
        lock.unlock();
    }

    @Override
    public void onPlayerState(long sequenceNumber, Player playerState) {
        lock.lock();
        if (player.getId().equals(playerState.getId())) {
            playerInputLog.discardLogUntil(sequenceNumber);
            playerController.setNextState(playerState);
            inputQueue.addAll(playerInputLog.getInputLog());
        } else {
            world.getPlayerController(playerState.getId().toString())
                    .setNextState(playerState);
        }
        lock.unlock();
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
