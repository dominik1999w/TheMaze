package service;

import java.util.HashMap;
import java.util.Map;
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
import player.RemotePlayer;

public class GameService extends TheMazeGrpc.TheMazeImplBase {

    private final Logger logger;

    private final Map<String, RemotePlayer> connectedPlayers = new HashMap<>();

    public GameService(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void connect(ConnectRequest request, StreamObserver<ConnectReply> responseObserver) {
        logger.info("Connect from " + request.getId());

        // update the simulation world
        connectedPlayers.put(request.getId(), new RemotePlayer());
        // reply with current game state
        ConnectReply reply = ConnectReply.newBuilder().setCount(connectedPlayers.size()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<GameStateRequest> syncGameState(StreamObserver<GameStateResponse> responseObserver) {
        return new StreamObserver<GameStateRequest>() {
            @Override
            public void onNext(GameStateRequest value) {
                PlayerState source = value.getPlayer();
                RemotePlayer player = connectedPlayers.getOrDefault(source.getId(), new RemotePlayer());
                player.setPosition(source.getX(), source.getY());

                GameStateResponse.Builder response = GameStateResponse.newBuilder();
                int i = 0;
                for (Map.Entry<String, RemotePlayer> connectedPlayer : connectedPlayers.entrySet()) {
                    response.setPlayers(i++, PlayerState.newBuilder()
                            .setId(connectedPlayer.getKey())
                            .setX(connectedPlayer.getValue().getX())
                            .setY(connectedPlayer.getValue().getY())
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
