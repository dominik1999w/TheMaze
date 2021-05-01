package connection.map;

import java.util.UUID;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import lib.map.MapGrpc;
import lib.map.StateRequest;
import lib.map.StateResponse;

public class GrpcMapClient implements MapClient {

    private final MapGrpc.MapBlockingStub blockingStub;
    private final MapGrpc.MapStub asyncStub;

    private final UUID id;
    private int length = 5;
    private int seed = 0;
    private boolean isHost = false;

    private StreamObserver<StateRequest> stateRequestStream;

    public GrpcMapClient(ManagedChannel channel) {
        this.id = UUID.randomUUID();

        this.blockingStub = MapGrpc.newBlockingStub(channel);
        this.asyncStub = MapGrpc.newStub(channel);
    }

    @Override
    public void connect() {
        StateRequest request = StateRequest.newBuilder()
                .setId(id.toString())
                .setLength(length)
                .setSeed(seed)
                .build();

        stateRequestStream = asyncStub.syncGameState(new StreamObserver<StateResponse>() {
            @Override
            public void onNext(StateResponse value) {
                length = value.getLength();
                seed = value.getSeed();
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });

        isHost = blockingStub.connect(request).getIsHost();
    }

    @Override
    public void syncState() {
        StateRequest request = StateRequest.newBuilder()
                .setId(id.toString())
                .setLength(length)
                .setSeed(seed)
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
}
