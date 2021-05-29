package service;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import connection.CallKey;
import entity.bullet.Bullet;
import entity.bullet.BulletController;
import entity.bullet.BulletHitbox;
import entity.player.Player;
import entity.player.PlayerHitbox;
import entity.player.controller.InputPlayerController;
import entity.player.controller.PlayerController;
import game.Game;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lib.connection.BulletState;
import lib.connection.GameStateRequest;
import lib.connection.GameStateResponse;
import lib.connection.LocalPlayerInput;
import lib.connection.PlayerState;
import lib.connection.TheMazeGrpc;
import lib.map.Position;
import map.generator.MapGenerator;
import physics.CollisionWorld;
import util.ClientsInputLog;
import util.GRpcMapper;
import util.Point2D;
import util.Timestamp;
import world.World;

public class GameService extends TheMazeGrpc.TheMazeImplBase {
    private static final Logger logger = Logger.getLogger(GameService.class.getName());

    private CollisionWorld collisionWorld;
    private World<InputPlayerController> world;

    private final ClientsInputLog inputLog;

    private final Map<StreamObserver<GameStateResponse>, UUID> responseObservers = new HashMap<>();

    public GameService() {
        this.inputLog = new ClientsInputLog();
        logger.setLevel(Level.WARNING);
    }

    private final Lock queueLock = new ReentrantLock();
    private final Queue<Timestamp<GameStateRequest>> requestQueue = new ArrayDeque<>();

    public void dispatchMessages(ClientRequestHandler requestHandler) {
        queueLock.lock();
        while (!requestQueue.isEmpty()) {
            Timestamp<GameStateRequest> tRequest = requestQueue.poll();
            GameStateRequest request = tRequest.get();
            LocalPlayerInput source = request.getPlayer();
            requestHandler.onClientRequest(
                    request.getSequenceNumber(),
                    tRequest.getTimestamp(),
                    UUID.fromString(source.getId()),
                    GRpcMapper.playerInput(source)
            );
            inputLog.onInputProcessed(UUID.fromString(source.getId()), request.getSequenceNumber());
            logger.log(Level.INFO,
                    "Last acknowledged input for {0}: {1}", new Object[]{
                            source.getId(), request.getSequenceNumber()
                    });
        }
        queueLock.unlock();
    }

    private final Set<StreamObserver<GameStateResponse>> disconnectedObservers = new HashSet<>();

    @Override
    public StreamObserver<GameStateRequest> syncGameState(StreamObserver<GameStateResponse> responseObserver) {
        onPlayerJoined(CallKey.PLAYER_ID.get(), responseObserver);
        ((ServerCallStreamObserver<GameStateResponse>) responseObserver)
                .setOnCancelHandler(() -> disconnectedObservers.add(responseObserver));

        return new StreamObserver<GameStateRequest>() {
            @Override
            public void onNext(GameStateRequest value) {
                Timestamp<GameStateRequest> tRequest = new Timestamp<>(value);
                queueLock.lock();
                requestQueue.add(tRequest);
                queueLock.unlock();
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "SyncGameState failed: {0}", t);
                //responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    public void broadcastGameState(long timestamp) {
        handleDisconnectedObservers();

        GameStateResponse.Builder response = GameStateResponse.newBuilder();
        response.setTimestamp(timestamp);
        for (java.util.Map.Entry<UUID, ? extends PlayerController> connectedPlayer : world.getConnectedPlayers()) {
            UUID id = connectedPlayer.getKey();
            PlayerController controller = connectedPlayer.getValue();
            response.addPlayers(PlayerState.newBuilder()
                    .setSequenceNumber(inputLog.getLastProcessedInput(id))
                    .setId(id.toString())
                    .setPositionX(controller.getPlayerPosition().x())
                    .setPositionY(controller.getPlayerPosition().y())
                    .setRotation(controller.getPlayerRotation())
                    .build());
        }
        world.getBullet().ifPresent(cachedBullet -> {
            UUID playerID = cachedBullet.getShooterID();
            Bullet bullet = cachedBullet.getController().getBullet();
            response.setBullet(BulletState.newBuilder()
                    .setId(bullet.getId().toString())
                    .setPlayerId(playerID.toString())
                    .setPositionX(bullet.getPosition().x())
                    .setPositionY(bullet.getPosition().y())
                    .setRotation(bullet.getRotation())
                    .build());
        });
        GameStateResponse stateResponse = response.build();

        for (Map.Entry<StreamObserver<GameStateResponse>, UUID> entry : responseObservers.entrySet()) {
            StreamObserver<GameStateResponse> responseObserver = entry.getKey();
            try {
                responseObserver.onNext(stateResponse);
            } catch (StatusRuntimeException e) {
                logger.log(Level.INFO,
                        "Player {0} disconnected", entry.getValue());
            }
        }
    }

    public void initializeWorld(Game game, int length, int seed, Map<UUID, Position> positions) {
        MapGenerator mapGenerator = new MapGenerator(length);
        map.Map map = mapGenerator.generateMap(seed);
        collisionWorld = new CollisionWorld(map);

        world = new World<>(
                InputPlayerController::new,
                (playerID, bullet) -> new BulletController(bullet));

        world.subscribeOnPlayerAdded(newPlayer -> collisionWorld.addPlayerHitbox(new PlayerHitbox(newPlayer, world)));
        world.subscribeOnPlayerRemoved(collisionWorld::removePlayerHitbox);
        world.subscribeOnBulletAdded((shooterID, newBullet) -> collisionWorld.setBulletHitbox(new BulletHitbox(shooterID, newBullet, world)));
        world.subscribeOnBulletRemoved(collisionWorld::removeBulletHitbox);
        world.subscribeOnRoundResult(game::endRound);

        for (Map.Entry<UUID, Position> entry : positions.entrySet()) {
            Position pos = entry.getValue();
            world.getPlayerController(entry.getKey(), new Point2D(pos.getPositionX(), pos.getPositionY()));
        }
    }

    public void startNewRound(Map<UUID, Position> positions) {
        queueLock.lock();
        requestQueue.clear();
        queueLock.unlock();

        for (Map.Entry<UUID, Position> entry : positions.entrySet()) {
            Position pos = entry.getValue();
            Player player = world.getPlayerController(entry.getKey()).getPlayer();
            player.setPosition(new Point2D(pos.getPositionX(), pos.getPositionY()));
            player.setRotation(0);
            player.revive(); // temporary
        }
        world.assignBulletRandomly();
    }

    private void onPlayerJoined(UUID playerID, StreamObserver<GameStateResponse> responseObserver) {
        queueLock.lock();
        responseObservers.put(responseObserver, playerID);

        world.onPlayerJoined(playerID);

        queueLock.unlock();
    }

    private void handleDisconnectedObservers() {
        for (StreamObserver<GameStateResponse> observer : disconnectedObservers) {
            UUID playerID = responseObservers.remove(observer);
            if (playerID != null)
                world.removePlayerController(playerID);
            logger.log(Level.INFO,
                    "Player {0} removed from the world", playerID);
        }
        disconnectedObservers.clear();
    }

    public CollisionWorld getCollisionWorld() {
        return collisionWorld;
    }

    public World<InputPlayerController> getWorld() {
        return world;
    }
}
