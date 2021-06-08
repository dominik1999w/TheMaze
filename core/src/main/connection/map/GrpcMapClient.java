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
import lib.map.MapStateResponse;
import lib.map.NameRequest;
import lib.map.NameResponse;
import lib.map.NamesResponse;
import lib.map.StateRequest;
import lib.map.StateResponse;
import util.Point2D;

public class GrpcMapClient implements MapClient {

    private final MapGrpc.MapBlockingStub blockingStub;
    private final MapGrpc.MapStub asyncStub;
    private final ManagedChannel channel;

    private UUID id;
    private String name;

    private StreamObserver<StateRequest> stateRequestStream;

    private final Lock queueLock = new ReentrantLock();
    private final Queue<StateResponse> responseQueue = new ArrayDeque<>();

    public GrpcMapClient(ManagedChannel channel) {
        this.channel = channel;
        this.blockingStub = MapGrpc.newBlockingStub(channel);
        this.asyncStub = MapGrpc.newStub(channel);
    }

    @Override
    public void dispatchMessages(ServerResponseHandler responseHandler) {
        queueLock.lock();
        while (!responseQueue.isEmpty()) {
            StateResponse response = responseQueue.poll();
            MapStateResponse mapResponse = response.getMapResponse();
            NamesResponse namesResponse = response.getNamesResponse();

            responseHandler.updateClientsNames(namesResponse.getNamesMap());
            responseHandler.updateInitialPosition(
                    new Point2D(mapResponse.getPosition().getPositionX(), mapResponse.getPosition().getPositionY())
            );
            if (mapResponse.getIsHost()) {
                responseHandler.displayAdminUI();
            } else {
                responseHandler.updateMap(mapResponse.getLength(), mapResponse.getSeed());
            }
            if (mapResponse.getStarted()) {
                responseHandler.startGame(mapResponse.getLength(), mapResponse.getSeed(), mapResponse.getIsHost());
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
                }));
        NameRequest request = NameRequest.newBuilder().setId(id.toString()).build();
        NameResponse response = blockingStub.connect(request);
        this.name = response.getName();
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

    @Override
    public void disconnect() {
        ((ClientCallStreamObserver<StateRequest>) stateRequestStream).cancel("Disconnected", null);
        try {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }

}