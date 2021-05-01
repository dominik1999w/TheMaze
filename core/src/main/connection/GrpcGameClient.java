package connection;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import entity.player.Player;
import entity.player.PlayerInput;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import lib.connection.GameStateRequest;
import lib.connection.GameStateResponse;
import lib.connection.LocalPlayerInput;
import lib.connection.TheMazeGrpc;
import util.Point2D;

public class GrpcGameClient implements GameClient {

    private final TheMazeGrpc.TheMazeBlockingStub blockingStub;
    private final TheMazeGrpc.TheMazeStub asyncStub;

    private UUID id;

    private StreamObserver<GameStateRequest> gameStateRequestStream;

    private final Lock queueLock = new ReentrantLock();
    private final Queue<GameStateResponse> responseQueue = new ArrayDeque<>();

    public GrpcGameClient(ManagedChannel channel) {
        this.blockingStub = TheMazeGrpc.newBlockingStub(channel);
        this.asyncStub = TheMazeGrpc.newStub(channel);
    }

    @Override
    public void dispatchMessages(ServerResponseHandler responseHandler) {
        queueLock.lock();
        /*System.out.println("Dispatching messages: " + responseQueue.size());
        while (!responseQueue.isEmpty()) {
            GameStateResponse response = responseQueue.poll();
            response.getPlayersList().forEach(playerState -> {
                responseHandler.onPlayerState(
                        playerState.getSequenceNumber(),
                        new Player(UUID.fromString(playerState.getId()),
                                new Point2D(playerState.getPositionX(), playerState.getPositionY()),
                                playerState.getRotation()
                        )
                );
            });
        }*/
        if (!responseQueue.isEmpty()) {
            GameStateResponse response = responseQueue.poll();
            response.getPlayersList().forEach(playerState -> {
                responseHandler.onPlayerState(
                        playerState.getSequenceNumber(),
                        new Player(UUID.fromString(playerState.getId()),
                                new Point2D(playerState.getPositionX(), playerState.getPositionY()),
                                playerState.getRotation()
                        )
                );
            });
            responseQueue.clear();
        }
        queueLock.unlock();
    }

    @Override
    public void connect() {
        gameStateRequestStream = asyncStub.syncGameState(new StreamObserver<GameStateResponse>() {
            @Override
            public void onNext(GameStateResponse value) {
                queueLock.lock();
                responseQueue.add(value);
                queueLock.unlock();
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    @Override
    public boolean syncState(long sequenceNumber, PlayerInput playerInput) {
        // if AFK (no reasonable input), then don't send it
        if (Math.abs(playerInput.getX()) < Float.MIN_VALUE &&
                Math.abs(playerInput.getY()) < Float.MIN_VALUE &&
                !playerInput.isShootPressed()) return false;

        GameStateRequest request = GameStateRequest.newBuilder()
                .setSequenceNumber(sequenceNumber)
                .setPlayer(LocalPlayerInput.newBuilder()
                        .setId(id.toString())
                        .setDelta(playerInput.getDelta())
                        .setInputX(playerInput.getX())
                        .setInputY(playerInput.getY())
                        .setShootPressed(playerInput.isShootPressed())
                        .build())
                .build();

        gameStateRequestStream.onNext(request);
        return true;
    }

    @Override
    public void enterGame(UUID id) {
        this.id = id;
    }
}
