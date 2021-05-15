package connection.map;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import connection.CallKey;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lib.map.MapGrpc;
import lib.map.Position;
import lib.map.StateRequest;
import lib.map.StateResponse;
import util.Point2D;

public class GrpcMapClient implements MapClient {

    private final MapGrpc.MapBlockingStub blockingStub;
    private final MapGrpc.MapStub asyncStub;
    private final ManagedChannel channel;

    private UUID id;
    private int length = 5;
    private int seed = 0;
    private boolean isHost = false;
    private Position startPos;
    private boolean gameStarted = false;

    private StreamObserver<StateRequest> stateRequestStream;

    public GrpcMapClient(ManagedChannel channel) {
        this.channel = channel;
        this.blockingStub = MapGrpc.newBlockingStub(channel);
        this.asyncStub = MapGrpc.newStub(channel);
    }

    @SuppressWarnings("CheckResult")
    @Override
    public void connect(UUID id) {
        this.id = id;

        StateRequest request = StateRequest.newBuilder()
                .setId(id.toString())
                .setLength(length)
                .setSeed(seed)
                .setStarted(gameStarted)
                .build();

        Context.current().withValue(CallKey.PLAYER_ID, id).run(() -> {
            stateRequestStream = asyncStub.syncGameState(new StreamObserver<StateResponse>() {
                @Override
                public void onNext(StateResponse value) {
                    length = value.getLength();
                    seed = value.getSeed();
                    startPos = value.getPosition();
                    gameStarted = value.getStarted();
                    isHost = value.getIsHost();
                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onCompleted() {

                }
            });
            blockingStub.connect(request);
        });
    }

    @Override
    public void disconnect() {
        ((ClientCallStreamObserver<StateRequest>) stateRequestStream).cancel("Disconnected", null);
        try {
            channel.shutdownNow().awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void syncState() {
        StateRequest request = StateRequest.newBuilder()
                .setId(id.toString())
                .setLength(length)
                .setSeed(seed)
                .setStarted(gameStarted)
                .build();

        stateRequestStream.onNext(request);
    }

    @Override
    public boolean isHost() {
        return isHost;
    }

    @Override
    public int getMapLength() {
        return length;
    }

    @Override
    public void setMapLength(int length) {
        this.length = length;
    }

    @Override
    public int getSeed() {
        return seed;
    }

    @Override
    public void setSeed(int seed) {
        this.seed = seed;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    @Override
    public Point2D getStartPos() {
        return new Point2D(startPos.getPositionX(), startPos.getPositionY());
    }
}