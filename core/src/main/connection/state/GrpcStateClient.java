package connection.state;

import com.google.protobuf.Empty;

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
import lib.state.StateGrpc;
import lib.state.StateResponse;

public class GrpcStateClient implements StateClient {

    private final StateGrpc.StateBlockingStub blockingStub;
    private final StateGrpc.StateStub asyncStub;
    private final ManagedChannel channel;

    private UUID id;

    private StreamObserver<Empty> stateRequestStream;

    private final Lock queueLock = new ReentrantLock();
    private final Queue<StateResponse> responseQueue = new ArrayDeque<>();

    public GrpcStateClient(ManagedChannel channel) {
        this.channel = channel;
        this.blockingStub = StateGrpc.newBlockingStub(channel);
        this.asyncStub = StateGrpc.newStub(channel);
    }

    @Override
    public void dispatchMessages(ServerResponseHandler handler) {
        queueLock.lock();
        while (!responseQueue.isEmpty()) {
            StateResponse response = responseQueue.poll();
            float timeToStartRound = response.getTimeToStartRound();
            handler.showGameCountdown(timeToStartRound);
        }
        queueLock.unlock();
    }

    @Override
    public void connect(UUID id) {
        this.id = id;

        Context.current().withValue(CallKey.PLAYER_ID, id).run(() ->
                stateRequestStream = asyncStub.syncState(new StreamObserver<StateResponse>() {
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
    }

    @Override
    public void disconnect() {
        ((ClientCallStreamObserver<Empty>) stateRequestStream).cancel("Disconnected", null);
        try {
            channel.shutdownNow().awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }
}
