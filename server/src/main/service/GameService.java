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
import util.ClientsInputLog;
import util.GRpcMapper;
import world.World;

public class GameService extends TheMazeGrpc.TheMazeImplBase {
    private static final Logger logger = Logger.getLogger(GameService.class.getName());

    private final World<?> world;
    private final ClientsInputLog inputLog;

    private final Map<StreamObserver<GameStateResponse>, UUID> responseObservers = new HashMap<>();

    public GameService(World<?> world) {
        this.world = world;
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
        world.onPlayerJoined(UUID.fromString(request.getId()));
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
        {
            ServerCallStreamObserver<GameStateResponse> serverCallStreamObserver =
                    (ServerCallStreamObserver<GameStateResponse>) responseObserver;
            serverCallStreamObserver.setOnCancelHandler(() ->
            {
                disconnectedObservers.add(responseObserver);
            });
        }

        return new StreamObserver<GameStateRequest>() {
            @Override
            public void onNext(GameStateRequest value) {
                queueLock.lock();
                responseObservers.putIfAbsent(responseObserver, UUID.fromString(value.getPlayer().getId()));
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

    public void broadcastGameState() {
        handleDisconnectedObservers();

        GameStateResponse.Builder response = GameStateResponse.newBuilder();
        for (java.util.Map.Entry<UUID, ? extends PlayerController> connectedPlayer : world.getConnectedPlayers()) {
            UUID id = connectedPlayer.getKey();
            PlayerController controller = connectedPlayer.getValue();
            response.addPlayers(PlayerState.newBuilder()
                    .setSequenceNumber(inputLog.getLastProcessedInput(id))
                    .setId(id.toString())
                    .setPositionX(controller.getPlayerPosition().x())
                    .setPositionY(controller.getPlayerPosition().y())
                    .setRotation(controller.getPlayerRotation())
                    .setBullet(BulletState.newBuilder()
                            .setFired(world.getBulletController(controller.getPlayer()) != null)
                            .build())
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

    private void handleDisconnectedObservers() {
        for (StreamObserver<GameStateResponse> observer : disconnectedObservers) {
            UUID playerID = responseObservers.remove(observer);
            world.removePlayerController(playerID);
            logger.log(Level.INFO,
                    "Player {0} removed from the world", playerID);
        }
        disconnectedObservers.clear();
    }
}
