package service;

import com.google.protobuf.Empty;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import entity.player.controller.AuthoritativePlayerController;
import entity.player.controller.PlayerController;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lib.connection.BulletState;
import lib.connection.ConnectReply;
import lib.connection.ConnectRequest;
import lib.connection.GameStateRequest;
import lib.connection.GameStateResponse;
import lib.connection.PlayerState;
import lib.connection.TheMazeGrpc;
import world.World;

public class GameService extends TheMazeGrpc.TheMazeImplBase {
    private static final Logger logger = Logger.getLogger(GameService.class.getName());

    public static final int SEED = 17;

    private final World<AuthoritativePlayerController> world;

    public GameService(World<AuthoritativePlayerController> world) {
        this.world = world;
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
    }

    @Override
    public StreamObserver<GameStateRequest> syncGameState(StreamObserver<GameStateResponse> responseObserver) {
        return new StreamObserver<GameStateRequest>() {
            @Override
            public void onNext(GameStateRequest value) {
                PlayerState source = value.getPlayer();
                AuthoritativePlayerController playerController = world.getPlayerController(source.getId());
                playerController.setNextPosition(source.getPositionX(), source.getPositionY());
                playerController.setNextRotation(source.getRotation());
                playerController.setNextFireBullet(source.getBullet().getFired());

                GameStateResponse.Builder response = GameStateResponse.newBuilder();
                for (Map.Entry<String, ? extends PlayerController> connectedPlayer : world.getConnectedPlayers()) {
                    response.addPlayers(PlayerState.newBuilder()
                            .setId(connectedPlayer.getKey())
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
