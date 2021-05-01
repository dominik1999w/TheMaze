package service;

import com.google.protobuf.Empty;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import entity.player.controller.PlayerController;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
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

    private final Map<String, StreamObserver<GameStateResponse>> responseObservers = new HashMap<>();

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
        logger.info("Connect from " + request.getId());
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
            logger.info(String.format(Locale.ENGLISH,
                    "Last acknowledged input for %s: %d", source.getId(), request.getSequenceNumber()));
        }
        queueLock.unlock();
    }

    @Override
    public StreamObserver<GameStateRequest> syncGameState(StreamObserver<GameStateResponse> responseObserver) {
        return new StreamObserver<GameStateRequest>() {
            @Override
            public void onNext(GameStateRequest value) {
                responseObservers.putIfAbsent(value.getPlayer().getId(), responseObserver);
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

    public void broadcastGameState() {
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

        Iterator<Map.Entry<String, StreamObserver<GameStateResponse>>> observerIterator =
                responseObservers.entrySet().iterator();
        while (observerIterator.hasNext()) {
            Map.Entry<String, StreamObserver<GameStateResponse>> entry = observerIterator.next();
            StreamObserver<GameStateResponse> responseObserver = entry.getValue();
            try {
                responseObserver.onNext(stateResponse);
            } catch (StatusRuntimeException e) {
                System.err.println(String.format(Locale.ENGLISH,
                        "Player %s disconnected", entry.getKey()));
                observerIterator.remove();
                world.removePlayerController(UUID.fromString(entry.getKey()));
            }
        }
    }
}
