package game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import connection.game.GameClient;
import connection.game.ServerResponseHandler;
import connection.util.PlayerInputLog;
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

    public GameScreen(UUID playerID, SpriteBatch batch, GameClient client, Map map, AssetManager assetManager) {
        this.batch = batch;
        this.playerInputLog = new PlayerInputLog();
        this.client = client;
        client.connect(playerID);

        this.camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        this.gameUI = new GameUI(assetManager);

        this.world = new World<>(AuthoritativePlayerController::new, BulletController::new);
        this.player = new Player(playerID, new Point2D(3.5f * MapConfig.BOX_SIZE, 2.5f * MapConfig.BOX_SIZE));
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

        this.debugDrawer = new DebugDrawer(camera, map, player);
    }

    @Override
    public void render(float delta) {
        // dispatch server messages
        client.dispatchMessages(new ServerResponseHandler() {
            @Override
            public void onActivePlayers(Collection<UUID> playerIDs) {
                world.getConnectedPlayers().forEach(playerEntry -> {
                    if (!playerIDs.contains(playerEntry.getKey())) {
                        world.removePlayerController(playerEntry.getKey());
                    }
                });
            }

            @Override
            public void onPlayerState(long sequenceNumber, Player playerState) {
                if (player.getId().equals(playerState.getId())) {
                    System.out.println(String.format(Locale.ENGLISH,
                            "Client: (%s, %d)    Server: (%s, %d)",
                            playerController.getPlayerPosition(), playerInputLog.getCurrentSequenceNumber(),
                            playerState.getPosition(), sequenceNumber));

                    playerInputLog.discardLogUntil(sequenceNumber);
                    playerController.setNextState(playerState);
                    for (PlayerInput playerInput : playerInputLog.getInputLog()) {
                        playerController.updateInput(playerInput);
                        playerController.update();
                        collisionWorld.update();
                    }
                } else {
                    world.getPlayerController(playerState.getId())
                            .setNextState(playerState);
                }
            }
        });

        // read player input
        PlayerInput playerInput = gameUI.readInput();
        playerInput.setDelta(delta);

        // check for AFK (no reasonable input)
        if (!playerInput.isEmpty()) {
            client.syncState(playerInputLog.getCurrentSequenceNumber(), playerInput);
            playerInputLog.log(playerInput);
            // update the player according to user input
            playerController.updateInput(playerInput);
            playerController.update();
        }

        // update the world
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
