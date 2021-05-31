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
import lib.map.MapStateRequest;
import lib.map.MapStateResponse;
import lib.map.NameRequest;
import lib.map.NameResponse;
import util.Point2D;

public class GrpcMapClient implements MapClient {

    private final MapGrpc.MapBlockingStub blockingStub;
    private final MapGrpc.MapStub asyncStub;
    private final ManagedChannel channel;

    private UUID id;
    private String name;

    private StreamObserver<MapStateRequest> stateRequestStream;

    private final Lock queueLock = new ReentrantLock();
    private final Queue<MapStateResponse> responseQueue = new ArrayDeque<>();

    public GrpcMapClient(ManagedChannel channel) {
        this.channel = channel;
        this.blockingStub = MapGrpc.newBlockingStub(channel);
        this.asyncStub = MapGrpc.newStub(channel);
    }

    @Override
    public void dispatchMessages(ServerResponseHandler responseHandler) {
        queueLock.lock();
        while (!responseQueue.isEmpty()) {
            MapStateResponse response = responseQueue.poll();

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

    @Override
    public String getUserName() {
        return name;
    }

    @Override
    public void connect(UUID id) {
        this.id = id;

        Context.current().withValue(CallKey.PLAYER_ID, id).run(() ->
                stateRequestStream = asyncStub.syncMapState(new StreamObserver<MapStateResponse>() {
                    @Override
                    public void onNext(MapStateResponse value) {
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
                }));
        NameRequest request = NameRequest.newBuilder().setId(id.toString()).build();
        NameResponse response = blockingStub.connect(request);
        this.name = response.getName();
    }

    @Override
    public void syncState(int length, int seed, boolean gameStarted) {
        MapStateRequest request = MapStateRequest.newBuilder()
                .setId(id.toString())
                .setLength(length)
                .setSeed(seed)
                .setStarted(gameStarted)
                .build();

        stateRequestStream.onNext(request);
    }

    @Override
    public void disconnect() {
        ((ClientCallStreamObserver<MapStateRequest>) stateRequestStream).cancel("Disconnected", null);
        try {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }

}