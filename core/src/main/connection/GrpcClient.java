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
import map.mapobjects.Player;
import player.RemotePlayer;
import experimental.World;

public class GrpcClient implements GameClient {

    private final ManagedChannel channel;
    private final TheMazeGrpc.TheMazeBlockingStub blockingStub;
    private final TheMazeGrpc.TheMazeStub asyncStub;

    private final Player player;
    private final World world;

    private final UUID id;

    private StreamObserver<GameStateRequest> gameStateRequestStream;

    public GrpcClient(Player player, World world, String host, int port) {
        this.player = player;
        this.world = world;
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
                //for (PlayerState playerState : value.getPlayersList()) {
                    //System.out.format("Player %s has position (%f,%f)\n", playerState.getId(), playerState.getX(), playerState.getY());
                //    if (!playerState.getId().equals(id.toString())) world.getPlayer(playerState.getId())
                //            .setPosition(playerState.getX(), playerState.getY());
                //}
                value.getPlayersList().stream()
                        .filter(playerState -> !playerState.getId().equals(id.toString()))
                        .forEach(playerState -> {
                            RemotePlayer player = world.getPlayer(playerState.getId());
                            player.setPosition(playerState.getPositionX(), playerState.getPositionY());
                            player.setRotation(playerState.getRotation());
                        });
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
                        .setPositionX(player.getPosition().x)
                        .setPositionY(player.getPosition().y)
                        .setRotation(player.getRotation())
                        .build())
                .build();
        gameStateRequestStream.onNext(request);
    }
}
