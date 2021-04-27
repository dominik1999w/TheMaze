package service;

import com.google.protobuf.Empty;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lib.map.ConnectReply;
import lib.map.MapGrpc;
import lib.map.StateRequest;
import lib.map.StateResponse;

public class MapService extends MapGrpc.MapImplBase {
    private static final Logger logger = Logger.getLogger(MapService.class.getName());

    private String host = null;
    private int lastSeed;
    private int lastLength;

    @Override
    public void handshake(Empty request, StreamObserver<Empty> responseObserver) {
        logger.info("Handshake from unknown");
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void connect(StateRequest request, StreamObserver<ConnectReply> responseObserver) {
        logger.info("Connect from " + request.getId());

        if (host == null) {
            host = request.getId();
            lastSeed = request.getSeed();
            lastLength = request.getLength();
        }

        ConnectReply reply = ConnectReply.newBuilder()
                .setIsHost(host.equals(request.getId()))
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<StateRequest> syncGameState(StreamObserver<StateResponse> responseObserver) {
        return new StreamObserver<StateRequest>() {
            @Override
            public void onNext(StateRequest value) {
                if (value.getId().equals(host)) {
                    lastLength = value.getLength();
                    lastSeed = value.getSeed();
                }

                responseObserver.onNext(
                        StateResponse.newBuilder()
                                .setLength(lastLength)
                                .setSeed(lastSeed)
                                .build()
                );
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "SyncGameState failed: {0}", Status.fromThrowable(t));
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
