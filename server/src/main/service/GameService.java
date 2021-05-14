package service;

import com.google.protobuf.Empty;

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
import entity.player.PlayerHitbox;
import entity.player.controller.InputPlayerController;
import entity.player.controller.PlayerController;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lib.connection.BulletState;
import lib.connection.ConnectReply;
import lib.connection.ConnectRequest;
import lib.connection.GameStateRequest;
import lib.connection.GameStateResponse;
import lib.connection.LocalPlayerInput;
import lib.connection.PlayerState;
import lib.connection.TheMazeGrpc;
import lib.map.Position;
import map.MapConfig;
import map.generator.MapGenerator;
import physics.CollisionWorld;
import time.Timer;
import util.ClientsInputLog;
import util.GRpcMapper;
import util.Point2D;
import world.World;

import static util.ServerConfig.SERVER_UPDATE_RATE;

public class GameService extends TheMazeGrpc.TheMazeImplBase {
    private static final Logger logger = Logger.getLogger(GameService.class.getName());

    private CollisionWorld collisionWorld;
    private World<InputPlayerController> world;

    private final ClientsInputLog inputLog;

    private final Map<StreamObserver<GameStateResponse>, UUID> responseObservers = new HashMap<>();

    private boolean enabled = false;

    public GameService() {
        this.inputLog = new ClientsInputLog();
    }

    @Override
    public void handshake(Empty request, StreamObserver<Empty> responseObserver) {
        logger.info("Handshake from unknown");
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void connect(ConnectRequest request, StreamObserver<ConnectReply> responseObserver) {
        logger.log(Level.INFO, "Connect from {0}", request.getId());
        responseObserver.onNext(ConnectReply.newBuilder().setSeed(0).build());
        responseObserver.onCompleted();
    }

    private final Lock queueLock = new ReentrantLock();
    private final Queue<GameStateRequest> requestQueue = new ArrayDeque<>();

    public void dispatchMessages(ClientRequestHandler requestHandler) {
        queueLock.lock();
        while (!requestQueue.isEmpty()) {
            GameStateRequest request = requestQueue.poll();
            LocalPlayerInput source = request.getPlayer();
            requestHandler.onClientRequest(
                    request.getSequenceNumber(),
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
        if (!enabled) {
            return new StreamObserver<GameStateRequest>() {
                @Override
                public void onNext(GameStateRequest value) {

                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onCompleted() {

                }
            };
        }

        onPlayerJoined(CallKey.PLAYER_ID.get(), responseObserver);
        ((ServerCallStreamObserver<GameStateResponse>) responseObserver)
                .setOnCancelHandler(() -> disconnectedObservers.add(responseObserver));

        return new StreamObserver<GameStateRequest>() {
            @Override
            public void onNext(GameStateRequest value) {
                queueLock.lock();
                requestQueue.add(value);
                queueLock.unlock();
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "SyncGameState failed: {0}", Status.fromThrowable(t));
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
        for (Map.Entry<UUID, BulletController> bulletEntry : world.getBullets()) {
            UUID playerID = bulletEntry.getKey();
            Bullet bullet = bulletEntry.getValue().getBullet();
            response.addBullets(BulletState.newBuilder()
                    .setId(bullet.getId().toString())
                    .setPlayerId(playerID.toString())
                    .setPositionX(bullet.getPosition().x())
                    .setPositionY(bullet.getPosition().y())
                    .setRotation(bullet.getRotation())
                    .build());
        }
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

    private void onPlayerJoined(UUID playerID, StreamObserver<GameStateResponse> responseObserver) {
        queueLock.lock();
        responseObservers.put(responseObserver, playerID);

        /* TODO: TEMPORARY workaround for working with GameService only */
        try {
            world.onPlayerJoined(playerID);
        } catch (NullPointerException exception) {
            world.getPlayerController(playerID, new Point2D(3.5f * MapConfig.BOX_SIZE, 2.5f * MapConfig.BOX_SIZE));
        }

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

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void initializeWorld(int length, int seed, HashMap<UUID, Position> positions) {
        MapGenerator mapGenerator = new MapGenerator(length);
        map.Map map = mapGenerator.generateMap(seed);
        collisionWorld = new CollisionWorld(map);

        world = new World<>(
                InputPlayerController::new,
                BulletController::new);

        world.subscribeOnPlayerAdded(newPlayer -> collisionWorld.addHitbox(new PlayerHitbox(newPlayer)));
        world.subscribeOnPlayerRemoved(collisionWorld::removeHitbox);
        world.subscribeOnBulletAdded(newBullet -> collisionWorld.addHitbox(new BulletHitbox(newBullet, world)));
        world.subscribeOnBulletRemoved(collisionWorld::removeHitbox);

        for (UUID id : positions.keySet()) {
            Position pos = positions.get(id);
            world.getPlayerController(id, new Point2D(pos.getPositionX(), pos.getPositionY()));
        }
    }

    public Thread gameThread() {
        return new Thread(() -> Timer.executeAtFixedRate(delta ->
        {
            if (!enabled) {
                return;
            }

            dispatchMessages((sequenceNumber, id, playerInput) ->
            {
                InputPlayerController playerController = world.getPlayerController(id);
                playerController.updateInput(playerInput);
                playerController.update();
                collisionWorld.update();
            });
            // TODO: rewrite: in world.update only bullets will be actually updated
            world.update(delta);
            collisionWorld.update();
            broadcastGameState(System.currentTimeMillis());
        }, 1.0f / SERVER_UPDATE_RATE));
    }
}
