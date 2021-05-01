package service;

import com.google.protobuf.Empty;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import entity.player.PlayerInput;
import entity.player.controller.InputPlayerController;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lib.connection.ConnectReply;
import lib.connection.ConnectRequest;
import lib.connection.GameStateRequest;
import lib.connection.GameStateResponse;
import lib.connection.LocalPlayerInput;
import lib.connection.TheMazeGrpc;
import world.World;

public class GameService extends TheMazeGrpc.TheMazeImplBase {
    private static final Logger logger = Logger.getLogger(GameService.class.getName());

    private final World<InputPlayerController> world;
    private final GameReplyService replyService;

    public GameService(World<InputPlayerController> world, GameReplyService replyService) {
        this.world = world;
        this.replyService = replyService;
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
        replyService.addResponseObserver(responseObserver);
        return new StreamObserver<GameStateRequest>() {
            @Override
            public void onNext(GameStateRequest value) {
                replyService.associateResponseObserverWith(responseObserver, value.getPlayer().getId());
                // process received PlayerInput
                LocalPlayerInput source = value.getPlayer();
                InputPlayerController playerController = world.getPlayerController(source.getId());
                playerController.notifyInput(new PlayerInput(source.getDelta(),
                        source.getInputX(), source.getInputY(), source.getShootPressed()));
                replyService.onInputProcessed(source.getId(), value.getSequenceNumber());
                logger.info(String.format(Locale.ENGLISH,
                        "Last acknowledged input for %s: %d", source.getId(), value.getSequenceNumber()));
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
