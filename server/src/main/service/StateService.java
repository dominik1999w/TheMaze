package service;

import com.google.protobuf.Empty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import connection.CallKey;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lib.state.StateGrpc;
import lib.state.StateResponse;

public class StateService extends StateGrpc.StateImplBase {
    private static final Logger logger = Logger.getLogger(StateService.class.getName());

    private final Map<StreamObserver<StateResponse>, UUID> clients = new HashMap<>();
    private final Set<StreamObserver<StateResponse>> disconnectedClients = new HashSet<>();

    public StateService() {
    }

    private final Lock lock = new ReentrantLock();

    @Override
    public StreamObserver<Empty> syncState(StreamObserver<StateResponse> responseObserver) {
        lock.lock();
        clients.put(responseObserver, CallKey.PLAYER_ID.get());
        ((ServerCallStreamObserver<StateResponse>) responseObserver)
                .setOnCancelHandler(() -> disconnectedClients.add(responseObserver));
        lock.unlock();

        return new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty value) {
                // currently server ignores clients' messages
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "SyncMapState failed: {0}", Status.fromThrowable(t));
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    public void broadcastPreRoundState(float delta) {
        handleDisconnectedClients();

        for (Map.Entry<StreamObserver<StateResponse>, UUID> entry : clients.entrySet()) {
            try {
                entry.getKey().onNext(
                        StateResponse.newBuilder()
                                .setTimeToStartRound(delta)
                                .setScore(0)
                                .build());
            } catch (StatusRuntimeException e) {
                logger.log(Level.INFO,
                        "Player {0} disconnected", entry.getValue());
            }
        }
    }

    private void handleDisconnectedClients() {
        for (StreamObserver<StateResponse> client : disconnectedClients) {
            UUID id = clients.remove(client);
            logger.log(Level.INFO,
                    "Player {0} removed from the world", id);
        }
        disconnectedClients.clear();
    }
}
