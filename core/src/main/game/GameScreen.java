package game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayDeque;
import java.util.Queue;

import connection.GameClient;
import connection.PlayerInputLog;
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

public class GameScreen extends ScreenAdapter {

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

        client.enterGame(player.getId());

        this.debugDrawer = new DebugDrawer(camera, map, player);
    }

    private final Queue<PlayerInput> inputQueue = new ArrayDeque<>();

    @Override
    public void render(float delta) {
        // dispatch server messages
        client.dispatchMessages(((sequenceNumber, playerState) ->
        {
            if (player.getId().equals(playerState.getId())) {
                playerInputLog.discardLogUntil(sequenceNumber);
                System.out.print("Client: " + playerController.getPlayer().getPosition() + playerInputLog.getCurrentSequenceNumber());
                System.out.println("    Server: " + playerState.getPosition() + sequenceNumber);
                playerController.setNextState(playerState);
                for (PlayerInput playerInput : playerInputLog.getInputLog()) {
                    playerController.notifyInput(playerInput);
                    playerController.update();
                    collisionWorld.update();
                }
            } else {
                System.out.println("WTF");
                world.getPlayerController(playerState.getId().toString())
                        .setNextState(playerState);
            }
        }));

        // read player input
        PlayerInput playerInput = gameUI.readInput();
        playerInput.setDelta(delta);

        // TODO: validate PlayerInput NOT inside syncState
        if (client.syncState(playerInputLog.getCurrentSequenceNumber(), playerInput)) {
            playerInputLog.log(playerInput);
            inputQueue.add(playerInput);
        }

        // update the world according to player input
        while (!inputQueue.isEmpty()) {
            playerController.notifyInput(inputQueue.poll());
            playerController.update();
            collisionWorld.update();
        }
        world.update(delta);
        collisionWorld.update();

        // update camera position
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
