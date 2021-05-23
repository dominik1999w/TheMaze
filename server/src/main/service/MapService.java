package service;

import com.google.protobuf.Empty;

import java.util.ArrayDeque;
import java.util.ArrayList;
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
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lib.map.MapGrpc;
import lib.map.Position;
import lib.map.StateRequest;
import lib.map.StateResponse;
import map.MapConfig;

public class MapService extends MapGrpc.MapImplBase {
    private static final Logger logger = Logger.getLogger(MapService.class.getName());

    private String host = null;
    private int lastSeed = 0;
    private int lastLength = 5;
    private boolean gameStarted = false;

    private final Map<StreamObserver<StateResponse>, UUID> clients = new HashMap<>();
    private final Set<StreamObserver<StateResponse>> disconnectedClients = new HashSet<>();

    public MapService() {
    }

    @Override
    public void handshake(Empty request, StreamObserver<Empty> responseObserver) {
        logger.info("Handshake from unknown");
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    private final Lock queueLock = new ReentrantLock();
    private final Queue<StateRequest> requestQueue = new ArrayDeque<>();

    @Override
    public void connect(StateRequest request, StreamObserver<Empty> responseObserver) {
        logger.info("Connect from " + request.getId());

        queueLock.lock();
        requestQueue.add(request);
        queueLock.unlock();

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    public void dispatchMessages() {
        queueLock.lock();
        while (!requestQueue.isEmpty()) {
            StateRequest request = requestQueue.poll();
            if (host == null) {
                host = request.getId();
            }

            if (request.getId().equals(host)) {
                lastLength = request.getLength();
                lastSeed = request.getSeed();
                gameStarted = request.getStarted();
            }
        }
        queueLock.unlock();
    }

    @Override
    public StreamObserver<StateRequest> syncMapState(StreamObserver<StateResponse> responseObserver) {
        queueLock.lock();
        clients.put(responseObserver, CallKey.PLAYER_ID.get());
        ((ServerCallStreamObserver<StateResponse>) responseObserver)
                .setOnCancelHandler(() -> {
                    if (CallKey.PLAYER_ID.get().toString().equals(host)) {
                        host = null;
                    }
                    disconnectedClients.add(responseObserver);
                });
        queueLock.unlock();

        return new StreamObserver<StateRequest>() {
            @Override
            public void onNext(StateRequest value) {
                queueLock.lock();
                requestQueue.add(value);
                queueLock.unlock();
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "SyncMapState failed: {0}", Status.fromThrowable(t));
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    public void broadcastMapState(StartGameHandler gameHandler) {
        handleDisconnectedClients();

        Map<UUID, Position> positions = new HashMap<>();
        for (Map.Entry<StreamObserver<StateResponse>, UUID> entry : clients.entrySet()) {
            UUID id = entry.getValue();
            positions.put(id, getStartingPosition(id.toString()));
        }

        if (gameStarted) {
            gameHandler.initializeGame(lastLength, lastSeed, positions);
        }

        for (Map.Entry<StreamObserver<StateResponse>, UUID> entry : clients.entrySet()) {
            try {
                entry.getKey().onNext(
                        StateResponse.newBuilder()
                                .setLength(lastLength)
                                .setSeed(lastSeed)
                                .setPosition(positions.get(entry.getValue()))
                                .setStarted(gameStarted)
                                .setIsHost(entry.getValue().toString().equals(host))
                                .build());
            } catch (StatusRuntimeException e) {
                logger.log(Level.INFO,
                        "Player {0} disconnected", entry.getValue());
            }
        }
    }

    private void handleDisconnectedClients() {
        for (StreamObserver<StateResponse> client : disconnectedClients) {
            UUID id = clients.remove(client);
            logger.log(Level.INFO,
                    "Player {0} removed from the world", id);
        }
        disconnectedClients.clear();
    }

    private Position getStartingPosition(String id) {
        float side = lastLength;
        float circuit = 4 * side;
        float gapBetweenPlayers = circuit / clients.size();
        int i = new ArrayList<>(clients.values()).indexOf(UUID.fromString(id));
        float distance = i * gapBetweenPlayers;

        Position.Builder result = Position.newBuilder();
        if (distance <= side - 1 || side > 4 * side - 1) {
            result.setPositionX(distance + 0.5f).setPositionY(0.5f);
        } else if (distance <= 2 * side - 1) {
            result.setPositionX(side - 0.5f).setPositionY(distance % side + 0.5f);
        } else if (distance <= 3 * side - 1) {
            result.setPositionX(side - distance % side - 0.5f).setPositionY(side - 0.5f);
        } else // distance <= 4 * side - 1
        {
            result.setPositionX(0.5f).setPositionY(side - distance % side - 0.5f);
        }

        result.setPositionX(result.getPositionX() * MapConfig.BOX_SIZE);
        result.setPositionY(result.getPositionY() * MapConfig.BOX_SIZE);

        return result.build();
    }

}
