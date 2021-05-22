package connection.map;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import connection.CallKey;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lib.map.MapGrpc;
import lib.map.StateRequest;
import lib.map.StateResponse;
import util.Point2D;

public class GrpcMapClient implements MapClient {

    private final MapGrpc.MapBlockingStub blockingStub;
    private final MapGrpc.MapStub asyncStub;
    private final ManagedChannel channel;

    private UUID id;

    private StreamObserver<StateRequest> stateRequestStream;

    private final Lock queueLock = new ReentrantLock();
    private final Queue<StateResponse> responseQueue = new ArrayDeque<>();

    public GrpcMapClient(ManagedChannel channel) {
        this.channel = channel;
        this.blockingStub = MapGrpc.newBlockingStub(channel);
        this.asyncStub = MapGrpc.newStub(channel);
    }

    public void dispatchMessages(ServerMapResponseHandler responseHandler) {
        queueLock.lock();
        while (!responseQueue.isEmpty()) {
            StateResponse response = responseQueue.poll();

            responseHandler.updateInitialPosition(
                    new Point2D(response.getPosition().getPositionX(), response.getPosition().getPositionY())
            );
            if (response.getIsHost()) {
                responseHandler.displayAdminUI();
            } else {
                responseHandler.updateMap(response.getLength(), response.getSeed());
            }
            if (response.getStarted()) {
                responseHandler.startGame(response.getLength(), response.getSeed(), response.getIsHost());
            }
        }
        queueLock.unlock();
    }

    @SuppressWarnings({"CheckResult", "ResultOfMethodCallIgnored"})
    @Override
    public void connect(UUID id) {
        this.id = id;

        StateRequest request = StateRequest.newBuilder()
                .setId(id.toString())
                .setLength(5)
                .setSeed(0)
                .setStarted(false)
                .build();

        Context.current().withValue(CallKey.PLAYER_ID, id).run(() -> {
            stateRequestStream = asyncStub.syncMapState(new StreamObserver<StateResponse>() {
                @Override
                public void onNext(StateResponse value) {
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
    public void syncState(int length, int seed, boolean gameStarted) {
        StateRequest request = StateRequest.newBuilder()
                .setId(id.toString())
                .setLength(length)
                .setSeed(seed)
                .setStarted(gameStarted)
                .build();

        stateRequestStream.onNext(request);
    }
}