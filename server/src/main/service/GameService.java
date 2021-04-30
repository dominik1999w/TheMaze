package service;

import com.google.protobuf.Empty;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import entity.player.controller.InputPlayerController;
import entity.player.controller.PlayerController;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lib.connection.BulletState;
import lib.connection.ConnectReply;
import lib.connection.ConnectRequest;
import lib.connection.GameStateRequest;
import lib.connection.GameStateResponse;
import lib.connection.LocalPlayerInput;
import lib.connection.PlayerState;
import lib.connection.TheMazeGrpc;
import timeout.TimeoutManager;
import world.World;

public class GameService extends TheMazeGrpc.TheMazeImplBase {
    private static final Logger logger = Logger.getLogger(GameService.class.getName());

    private final World<InputPlayerController> world;
    private final TimeoutManager timeoutManager;

    public GameService(World<InputPlayerController> world) {
        this.world = world;
        this.timeoutManager = new TimeoutManager(playerId -> {
            world.removePlayerController(playerId);
            logger.info("Timed out " + playerId);
        }, 1000);
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

    @Override
    public StreamObserver<GameStateRequest> syncGameState(StreamObserver<GameStateResponse> responseObserver) {
        return new StreamObserver<GameStateRequest>() {
            @Override
            public void onNext(GameStateRequest value) {
                // process input request
                LocalPlayerInput source = value.getPlayer();
                timeoutManager.notify(UUID.fromString(source.getId()));
                InputPlayerController playerController = world.getPlayerController(UUID.fromString(source.getId()));
                playerController.notifyInput(source.getInputX(), source.getInputY(), source.getShootPressed());

                // reply with game state
                GameStateResponse.Builder response = GameStateResponse.newBuilder();
                for (Map.Entry<UUID, ? extends PlayerController> connectedPlayer : world.getConnectedPlayers()) {
                    response.addPlayers(PlayerState.newBuilder()
                            .setId(connectedPlayer.getKey().toString())
                            .setPositionX(connectedPlayer.getValue().getPlayerPosition().x())
                            .setPositionY(connectedPlayer.getValue().getPlayerPosition().y())
                            .setRotation(connectedPlayer.getValue().getPlayerRotation())
                            .setBullet(BulletState.newBuilder()
                                    .setFired(world.getBulletController(connectedPlayer.getValue().getPlayer()) != null)
                                    .build())
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
