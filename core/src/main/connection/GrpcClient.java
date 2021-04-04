package connection;

import java.util.UUID;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lib.connection.ConnectRequest;
import lib.connection.GameStateRequest;
import lib.connection.GameStateResponse;
import lib.connection.PlayerState;
import lib.connection.TheMazeGrpc;
import renderable.PlayerView;

public class GrpcClient implements GameClient {

    private final ManagedChannel channel;
    private final TheMazeGrpc.TheMazeBlockingStub blockingStub;
    private final TheMazeGrpc.TheMazeStub asyncStub;

    private final PlayerView playerView;

    private final UUID id;

    private StreamObserver<GameStateRequest> gameStateRequestStream;

    public GrpcClient(PlayerView playerView, String host, int port) {
        this.playerView = playerView;
        this.id = UUID.randomUUID();

        this.channel = ManagedChannelBuilder.forTarget("dns:///" + host + ":" + port).usePlaintext().build();
        this.blockingStub = TheMazeGrpc.newBlockingStub(channel);
        this.asyncStub = TheMazeGrpc.newStub(channel);
    }

    @Override
    public int connect() {
        ConnectRequest request = ConnectRequest.newBuilder().setId(id.toString()).build();

        gameStateRequestStream = asyncStub.syncGameState(new StreamObserver<GameStateResponse>() {
            @Override
            public void onNext(GameStateResponse value) {
                for (PlayerState playerState : value.getPlayersList()) {
                    System.out.format("Player %s has position (%f,%f)\n", playerState.getId(), playerState.getX(), playerState.getY());
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });

        return blockingStub.connect(request).getCount();
    }

    @Override
    public void syncGameState() {
        GameStateRequest request = GameStateRequest.newBuilder()
                .setPlayer(PlayerState.newBuilder()
                        .setId(id.toString())
                        .setX(playerView.getPosition().x)
                        .setY(playerView.getPosition().y)
                        .build())
                .build();
        gameStateRequestStream.onNext(request);
    }
}
