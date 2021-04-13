package service;

import com.google.protobuf.Empty;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lib.connection.ConnectReply;
import lib.connection.ConnectRequest;
import lib.connection.GameStateRequest;
import lib.connection.GameStateResponse;
import lib.connection.PlayerState;
import lib.connection.TheMazeGrpc;
import entity.player.Player;

public class GameService extends TheMazeGrpc.TheMazeImplBase {

    private final Logger logger;

    private final Map<String, Player> connectedPlayers = new ConcurrentHashMap<>();

    public GameService(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void handshake(Empty request, StreamObserver<Empty> responseObserver) {
        logger.info("Handshake from unknown");
        responseObserver.onNext(Empty.newBuilder().build());
    }

    @Override
    public void connect(ConnectRequest request, StreamObserver<ConnectReply> responseObserver) {
        logger.info("Connect from " + request.getId());

        if (connectedPlayers.containsKey(request.getId())) {
            logger.warning(String.format(Locale.ENGLISH,
                    "Player %s has already connected", request.getId()));
            responseObserver.onError(new RuntimeException("Cannot connect() twice"));
            return;
        }

        // update the simulation world
        connectedPlayers.put(request.getId(), new Player());
        // reply with current game state
        ConnectReply reply = ConnectReply.newBuilder()
                .setCount(connectedPlayers.size())
                .setSeed(17)
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<GameStateRequest> syncGameState(StreamObserver<GameStateResponse> responseObserver) {
        return new StreamObserver<GameStateRequest>() {
            @Override
            public void onNext(GameStateRequest value) {
                PlayerState source = value.getPlayer();
                Player player = connectedPlayers.getOrDefault(source.getId(), new Player());
                player.setPosition(source.getPositionX(), source.getPositionY());
                player.setRotation(source.getRotation());

                GameStateResponse.Builder response = GameStateResponse.newBuilder();
                for (Map.Entry<String, Player> connectedPlayer : connectedPlayers.entrySet()) {
                    response.addPlayers(PlayerState.newBuilder()
                            .setId(connectedPlayer.getKey())
                            .setPositionX(connectedPlayer.getValue().getPosition().x())
                            .setPositionY(connectedPlayer.getValue().getPosition().y())
                            .setRotation(connectedPlayer.getValue().getRotation())
                            .build());
                }
                responseObserver.onNext(response.build());
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
}
