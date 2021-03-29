package service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import io.grpc.stub.StreamObserver;
import lib.connection.ConnectReply;
import lib.connection.ConnectRequest;
import lib.connection.TheMazeGrpc;

public class GameService extends TheMazeGrpc.TheMazeImplBase {

    private final Logger logger;

    private final List<UUID> connectedPlayers = new ArrayList<>();

    public GameService(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void connect(ConnectRequest request, StreamObserver<ConnectReply> responseObserver) {
        logger.info("Connect from " + request.getId());

        // update the simulation world
        connectedPlayers.add(UUID.fromString(request.getId()));
        // reply with current game state
        ConnectReply reply = ConnectReply.newBuilder().setCount(connectedPlayers.size()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
