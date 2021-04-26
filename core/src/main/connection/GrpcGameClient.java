package connection;

import java.util.UUID;

import entity.player.Player;
import entity.player.controller.AuthoritativePlayerController;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import lib.connection.BulletState;
import lib.connection.ConnectRequest;
import lib.connection.GameStateRequest;
import lib.connection.GameStateResponse;
import lib.connection.PlayerState;
import lib.connection.TheMazeGrpc;
import world.World;

public class GrpcGameClient implements GameClient {

    private final TheMazeGrpc.TheMazeBlockingStub blockingStub;
    private final TheMazeGrpc.TheMazeStub asyncStub;

    private Player player;
    private boolean bulletFired;
    private World<AuthoritativePlayerController> world;

    private final UUID id;

    private StreamObserver<GameStateRequest> gameStateRequestStream;

    public GrpcGameClient(ManagedChannel channel) {
        this.id = UUID.randomUUID();

        this.blockingStub = TheMazeGrpc.newBlockingStub(channel);
        this.asyncStub = TheMazeGrpc.newStub(channel);
    }

    @Override
    public void connect() {
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
                            AuthoritativePlayerController playerController = world.getPlayerController(playerState.getId());
                            playerController.setNextPosition(playerState.getPositionX(), playerState.getPositionY());
                            playerController.setNextRotation(playerState.getRotation());
                            playerController.setNextFireBullet(playerState.getBullet().getFired());
                        });
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    @Override
    public void syncState() {
        GameStateRequest request = GameStateRequest.newBuilder()
                .setPlayer(PlayerState.newBuilder()
                        .setId(id.toString())
                        .setPositionX(player.getPosition().x())
                        .setPositionY(player.getPosition().y())
                        .setRotation(player.getRotation())
                        .setBullet(BulletState.newBuilder().setFired(bulletFired).build())
                        .build())
                .build();

        bulletFired = false;
        gameStateRequestStream.onNext(request);
    }

    public void enterGame(Player player, World<AuthoritativePlayerController> world) {
        this.player = player;
        this.world = world;
        world.subscribeOnBulletAdded((player1, bullet) -> {
            if (player1.getId().equals(player.getId())) bulletFired = true;
        });
    }
}
