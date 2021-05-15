package connection.game;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import connection.CallKey;
import entity.player.PlayerInput;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import lib.connection.BulletState;
import lib.connection.GameStateRequest;
import lib.connection.GameStateResponse;
import lib.connection.LocalPlayerInput;
import lib.connection.PlayerState;
import lib.connection.TheMazeGrpc;
import util.GRpcMapper;

public class GrpcGameClient implements GameClient {

    private final TheMazeGrpc.TheMazeBlockingStub blockingStub;
    private final TheMazeGrpc.TheMazeStub asyncStub;
    private final ManagedChannel channel;

    private UUID id;

    private StreamObserver<GameStateRequest> gameStateRequestStream;

    private final Lock queueLock = new ReentrantLock();
    private final Queue<GameStateResponse> responseQueue = new ArrayDeque<>();

    public GrpcGameClient(ManagedChannel channel) {
        this.channel = channel;
        this.blockingStub = TheMazeGrpc.newBlockingStub(channel);
        this.asyncStub = TheMazeGrpc.newStub(channel);
    }

    @Override
    public void dispatchMessages(ServerResponseHandler responseHandler) {
        queueLock.lock();
        //System.out.println("Dispatching messages: " + responseQueue.size());
        while (!responseQueue.isEmpty()) {
            GameStateResponse response = responseQueue.poll();

            Collection<UUID> activePlayers = response.getPlayersList().stream()
                    .map(PlayerState::getId)
                    .map(UUID::fromString)
                    .collect(Collectors.toSet());
            responseHandler.onActivePlayers(activePlayers);

            Collection<UUID> activeBullets = response.getBulletsList().stream()
                    .map(BulletState::getId)
                    .map(UUID::fromString)
                    .collect(Collectors.toSet());
            responseHandler.onActiveBullets(activeBullets);

            response.getPlayersList().forEach(playerState ->
                    responseHandler.onPlayerState(
                            playerState.getId().equals(id.toString()) ? playerState.getSequenceNumber() : response.getTimestamp(),
                            GRpcMapper.playerState(playerState)
                    )
            );

            response.getBulletsList().forEach(bulletState ->
                    responseHandler.onBulletState(
                            UUID.fromString(bulletState.getPlayerId()),
                            GRpcMapper.bulletState(bulletState)
                    )
            );
        }
        queueLock.unlock();
    }

    @Override
    public void connect(UUID id) {
        this.id = id;

        Context.current().withValue(CallKey.PLAYER_ID, id).run(() -> {
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
        });
    }

    @Override
    public void syncState(long sequenceNumber, PlayerInput playerInput) {
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
    }

    @Override
    public void disconnect() {
        try {
            channel.shutdownNow().awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }
}
