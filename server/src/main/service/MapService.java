package service;

import com.google.protobuf.Empty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import connection.CallKey;
import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lib.map.ConnectReply;
import lib.map.MapGrpc;
import lib.map.Position;
import lib.map.StateRequest;
import lib.map.StateResponse;
import map.MapConfig;

public class MapService extends MapGrpc.MapImplBase {
    private static final Logger logger = Logger.getLogger(MapService.class.getName());
    private final GameService gameService;

    private String host = null;
    private int lastSeed;
    private int lastLength;
    private boolean gameStarted = false;

    private final Map<StreamObserver<StateResponse>, UUID> clients = new HashMap<>();
    private final Set<StreamObserver<StateResponse>> disconnectedClients = new HashSet<>();

    public MapService(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void handshake(Empty request, StreamObserver<Empty> responseObserver) {
        logger.info("Handshake from unknown");
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void connect(StateRequest request, StreamObserver<ConnectReply> responseObserver) {
        logger.info("Connect from " + request.getId());

        if (host == null) {
            host = request.getId();
            lastSeed = request.getSeed();
            lastLength = request.getLength();
        }

        ConnectReply reply = ConnectReply.newBuilder()
                .setIsHost(host.equals(request.getId()))
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<StateRequest> syncGameState(StreamObserver<StateResponse> responseObserver) {
        clients.put(responseObserver, CallKey.PLAYER_ID.get());
        ((ServerCallStreamObserver<StateResponse>) responseObserver)
                .setOnCancelHandler(() -> disconnectedClients.add(responseObserver));

        return new StreamObserver<StateRequest>() {
            @Override
            public void onNext(StateRequest value) {
                if (value.getId().equals(host)) {
                    lastLength = value.getLength();
                    lastSeed = value.getSeed();
                    gameStarted = value.getStarted();
                }

                handleDisconnectedClients();

                Position position = getStartingPosition(value.getId());

                responseObserver.onNext(
                        StateResponse.newBuilder()
                                .setLength(lastLength)
                                .setSeed(lastSeed)
                                .setPosition(position)
                                .setStarted(gameStarted)
                                .build()
                );

                if (gameStarted && value.getId().equals(host)) {
                    logger.info("a ti");
                    HashMap<UUID, Position> positions = new HashMap<>();
                    for (Map.Entry<StreamObserver<StateResponse>, UUID> entry : clients.entrySet()) {
                        UUID id = entry.getValue();
                        positions.put(id, getStartingPosition(id.toString()));
                    }

                    gameService.initializeWorld(lastLength, lastSeed, positions);
                    gameService.setEnabled(true);
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "SyncGameState failed: {0}", Status.fromThrowable(t));
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
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
