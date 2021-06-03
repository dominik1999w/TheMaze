package service;

import com.google.protobuf.Empty;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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

    private final Map<StreamObserver<StateResponse>, UUID> clients = new ConcurrentHashMap<>();
    private final Set<StreamObserver<StateResponse>> disconnectedClients = ConcurrentHashMap.newKeySet();

    public StateService() {
    }

    @Override
    public StreamObserver<Empty> syncState(StreamObserver<StateResponse> responseObserver) {
        clients.put(responseObserver, CallKey.PLAYER_ID.get());
        ((ServerCallStreamObserver<StateResponse>) responseObserver)
                .setOnCancelHandler(() -> disconnectedClients.add(responseObserver));

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

    public void broadcastState(float delta, Map<String, Integer> points, boolean gameEnded) {
        handleDisconnectedClients();
        for (Map.Entry<StreamObserver<StateResponse>, UUID> entry : clients.entrySet()) {
            try {
                entry.getKey().onNext(
                        StateResponse.newBuilder()
                                .setTimeToStartRound(delta)
                                .setGameEnded(gameEnded)
                                .putAllScores(points)
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
