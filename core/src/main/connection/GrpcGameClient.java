package connection;

import java.util.UUID;

import entity.player.controller.AuthoritativePlayerController;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import lib.connection.ConnectRequest;
import lib.connection.GameStateRequest;
import lib.connection.GameStateResponse;
import lib.connection.LocalPlayerInput;
import lib.connection.TheMazeGrpc;
import timeout.TimeoutManager;
import world.World;

public class GrpcGameClient implements GameClient {

    private final TheMazeGrpc.TheMazeBlockingStub blockingStub;
    private final TheMazeGrpc.TheMazeStub asyncStub;

    private World<AuthoritativePlayerController> world;
    private TimeoutManager timeoutManager;

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
                value.getPlayersList().forEach(playerState -> {
                    timeoutManager.notify(UUID.fromString(playerState.getId()));
                    if (playerState.getId().equals(id.toString())) {
                        // transfer playerState values to localPlayerController for interpolation
                    } else {
                        AuthoritativePlayerController playerController = world.getPlayerController(UUID.fromString(playerState.getId()));
                        playerController.setNextPosition(playerState.getPositionX(), playerState.getPositionY());
                        playerController.setNextRotation(playerState.getRotation());
                        playerController.setNextFireBullet(playerState.getBullet().getFired());
                    }
                });
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });

        blockingStub.connect(request);
    }

    // NOTE: temporary
    private boolean isShootPressed = false;
    private float inputX = 0;
    private float inputY = 0;

    @Override
    public void notifyInput(float x, float y, boolean shootPressed) {
        this.inputX = x;
        this.inputY = y;
        if (shootPressed) this.isShootPressed = true;
    }

    @Override
    public void syncState() {
        GameStateRequest request = GameStateRequest.newBuilder()
                .setPlayer(LocalPlayerInput.newBuilder()
                        .setId(id.toString())
                        .setInputX(inputX)
                        .setInputY(inputY)
                        .setShootPressed(isShootPressed)
                        .build())
                .build();

        isShootPressed = false;
        gameStateRequestStream.onNext(request);
    }

    @Override
    public void enterGame(World<AuthoritativePlayerController> world) {
        this.world = world;
        this.timeoutManager = new TimeoutManager(playerId -> {
            world.removePlayerController(playerId);
            System.out.println("Timed out " + playerId);
        },1000);
    }
}
