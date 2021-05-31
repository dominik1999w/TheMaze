package service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
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
import lib.map.MapStateRequest;
import lib.map.MapStateResponse;
import lib.map.NameRequest;
import lib.map.NameResponse;
import lib.map.Position;
import map.MapConfig;
import util.RandomNames;

public class MapService extends MapGrpc.MapImplBase {
    private static final Logger logger = Logger.getLogger(MapService.class.getName());

    private AtomicReference<String> host = new AtomicReference<>("");
    private int lastSeed = 0;
    private int lastLength = 5;
    private boolean gameStarted = false;

    private final Map<StreamObserver<MapStateResponse>, UUID> clients = new ConcurrentHashMap<>();
    private final Map<StreamObserver<MapStateResponse>, UUID> disconnectedClients = new ConcurrentHashMap<>();
    private final Map<UUID, String> names = new ConcurrentHashMap<>();

    public MapService() {
    }

    private final Lock queueLock = new ReentrantLock();
    private final Queue<MapStateRequest> requestQueue = new ArrayDeque<>();

    public void dispatchMessages() {
        queueLock.lock();
        while (!requestQueue.isEmpty()) {
            MapStateRequest request = requestQueue.poll();
            if (host.get().equals("") && !disconnectedClients.containsValue(UUID.fromString(request.getId()))) {
                host.set(request.getId());
            }
            if (request.getId().equals(host.get())) {
                lastLength = request.getLength();
                lastSeed = request.getSeed();
                gameStarted = request.getStarted();
            }
        }
        queueLock.unlock();
    }

    @Override
    public void connect(NameRequest request, StreamObserver<NameResponse> responseObserver) {
        String name = RandomNames.getNextName();
        names.put(UUID.fromString(request.getId()), name);
        responseObserver.onNext(NameResponse.newBuilder().setName(name).build());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<MapStateRequest> syncMapState(StreamObserver<MapStateResponse> responseObserver) {
        clients.put(responseObserver, CallKey.PLAYER_ID.get());

        ((ServerCallStreamObserver<MapStateResponse>) responseObserver)
                .setOnCancelHandler(() -> {
                    if (CallKey.PLAYER_ID.get().toString().equals(host.get())) {
                        host.set("");
                    }
                    disconnectedClients.put(responseObserver, CallKey.PLAYER_ID.get());
                });

        return new StreamObserver<MapStateRequest>() {
            @Override
            public void onNext(MapStateRequest value) {
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
        for (Map.Entry<StreamObserver<MapStateResponse>, UUID> entry : clients.entrySet()) {
            UUID id = entry.getValue();
            positions.put(id, getStartingPosition(id.toString()));
        }

        if (gameStarted) {
            gameHandler.initializeGame(lastLength, lastSeed, positions);
        }

        for (Map.Entry<StreamObserver<MapStateResponse>, UUID> entry : clients.entrySet()) {
            try {
                entry.getKey().onNext(
                        MapStateResponse.newBuilder()
                                .setLength(lastLength)
                                .setSeed(lastSeed)
                                .setPosition(positions.get(entry.getValue()))
                                .setStarted(gameStarted)
                                .setIsHost(entry.getValue().toString().equals(host.get()))
                                .build());
            } catch (StatusRuntimeException e) {
                logger.log(Level.INFO,
                        "Player {0} disconnected", entry.getValue());
            }
        }
    }

    public Map<UUID, String> getNames() {
        return names;
    }

    private void handleDisconnectedClients() {
        for (StreamObserver<MapStateResponse> client : disconnectedClients.keySet()) {
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
