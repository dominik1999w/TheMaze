package service;

import com.google.protobuf.Empty;

import java.util.ArrayDeque;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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

    private final GameReplyService replyService;

    public GameService(GameReplyService replyService) {
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
                    new PlayerInput(
                            source.getDelta(),
                            source.getInputX(),
                            source.getInputY(),
                            source.getShootPressed()
                    )
            );
            replyService.onInputProcessed(source.getId(), request.getSequenceNumber());
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
                replyService.addResponseObserver(value.getPlayer().getId(), responseObserver);
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
}
