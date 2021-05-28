package game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import connection.game.GameClient;
import connection.state.StateClient;
import connection.util.PlayerInputLog;
import entity.bullet.Bullet;
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

    private final GameClient gameClient;
    private final StateClient stateClient;

    private final PlayerInputLog playerInputLog;

    private final BitmapFont bitmapFont;
    private final DebugDrawer debugDrawer;

    public GameScreen(UUID playerID, SpriteBatch batch, GameClient gameClient, StateClient stateClient, Point2D initialPosition, Map map, AssetManager assetManager) {
        this.batch = batch;
        this.playerInputLog = new PlayerInputLog();

        this.gameClient = gameClient;
        this.stateClient = stateClient;

        gameClient.connect(playerID);
        stateClient.connect(playerID);

        this.world = new World<>(AuthoritativePlayerController::new, (playerID1, bullet) -> new BulletController(bullet));

        this.player = new Player(playerID, initialPosition);
        this.playerController = new LocalPlayerController(player, world);
        this.collisionWorld = new CollisionWorld(map);
        // Uncomment this for CLIENT-SIDE PLAYER-MAP COLLISION HANDLING
        //world.subscribeOnPlayerAdded(newPlayer -> collisionWorld.addHitbox(new PlayerHitbox(newPlayer)));
        world.subscribeOnBulletAdded(newBullet -> collisionWorld.setBulletHitbox(new BulletHitbox(newBullet, world)));
        world.subscribeOnBulletRemoved(collisionWorld::removeHitbox);
        collisionWorld.addPlayerHitbox(new PlayerHitbox(player));

        this.camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.worldView = new WorldView(world, map, camera, player, assetManager);

        int mapWidth = 10; // temporary: number of boxes horizontal-wise
        camera.zoom = mapWidth * MapConfig.BOX_SIZE / (float) Gdx.graphics.getWidth();
        camera.update();

        this.gameUI = new GameUI(assetManager);
        gameUI.build();

        this.bitmapFont = new BitmapFont();
        this.debugDrawer = new DebugDrawer(camera, map, player);
    }

    boolean newRoundStarting = true;
    int countDownTime;

    @Override
    public void render(float delta) {
        updateCountDown();

        if (!newRoundStarting) {
            // dispatch server messages
            gameClient.dispatchMessages(new GameClient.ServerResponseHandler() {
                @Override
                public void onActivePlayers(Collection<UUID> playerIDs) {
                    // NOTE: need iterator here to avoid ConcurrentModificationException
                    Iterator<java.util.Map.Entry<UUID, AuthoritativePlayerController>> iterator =
                            world.getConnectedPlayers().iterator();
                    while (iterator.hasNext()) {
                        UUID playerID = iterator.next().getKey();
                        if (!playerIDs.contains(playerID)) {
                            world.removePlayerController(playerID);
                        }
                    }
                }

                @Override
                public void onPlayerState(long sequenceNumber, long timestamp, Player playerState) {
                    if (player.getId().equals(playerState.getId())) {
                    /*System.out.println(String.format(Locale.ENGLISH,
                            "Client: (%s, %d)    Server: (%s, %d)",
                            playerController.getPlayerPosition(), playerInputLog.getCurrentSequenceNumber(),
                            playerState.getPosition(), sequenceNumber));*/

                        playerInputLog.discardLogUntil(sequenceNumber);
                        playerController.setNextState(0, playerState);
                        for (PlayerInput playerInput : playerInputLog.getInputLog()) {
                            playerController.updateInput(playerInput);
                            playerController.update();
                            collisionWorld.onPlayerMoved(player.getId(), timestamp, playerInput.getDelta());
                        }
                    } else {
                        world.getPlayerController(playerState.getId(), playerState.getPosition())
                                .setNextState(sequenceNumber, playerState);
                    }
                }

                @Override
                public void onBulletState(UUID playerID, Bullet bulletState) {
                    //if (player.getId().equals(playerID)) return;

                    if (playerID == null) {
                        world.onBulletDied();
                    } else {
                        world.onBulletFired(playerID, bulletState);
                    }
                }
            });

            // read player input
            PlayerInput playerInput = gameUI.readInput();
            playerInput.setDelta(delta);

            // check for AFK (no reasonable input)
            if (!playerInput.isEmpty()) {
                gameClient.syncState(playerInputLog.getCurrentSequenceNumber(), playerInput);
                playerInputLog.log(playerInput);
                // update the player according to user input
                playerController.updateInput(playerInput);
                playerController.update();
                collisionWorld.onPlayerMoved(player.getId(), System.currentTimeMillis(), playerInput.getDelta());
            }

            // update the world
            world.update(delta);
            collisionWorld.update();
        }

        Gdx.gl.glClearColor(.5f, .5f, .5f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Point2D playerPosition = player.getPosition();
        camera.position.set(playerPosition.x(), playerPosition.y(), 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        worldView.render(batch);
        if (newRoundStarting) {
            bitmapFont.setColor(1, 1, 1, 1);
            bitmapFont.draw(batch, String.valueOf(countDownTime), playerPosition.x(), playerPosition.y());
        }
        batch.end();

        gameUI.render(delta);
    }

    private void updateCountDown() {
        stateClient.dispatchMessages(time -> {
            countDownTime = (int)time;
            newRoundStarting = (countDownTime > 0);
        });
    }

    @Override
    public void resize(int width, int height) {
        gameUI.resize(width, height);
    }

    @Override
    public void dispose() {
        gameUI.dispose();
        bitmapFont.dispose();
    }
}
