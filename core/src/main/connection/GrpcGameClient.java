package connection;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;

import entity.player.GameAuthoritativeListener;
import entity.player.Player;
import entity.player.PlayerInput;
import entity.player.controller.AuthoritativePlayerController;
import entity.player.controller.LocalPlayerController;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import lib.connection.ConnectRequest;
import lib.connection.GameStateRequest;
import lib.connection.GameStateResponse;
import lib.connection.LocalPlayerInput;
import lib.connection.TheMazeGrpc;
import util.Point2D;
import world.World;

public class GrpcGameClient implements GameClient {

    private final TheMazeGrpc.TheMazeBlockingStub blockingStub;
    private final TheMazeGrpc.TheMazeStub asyncStub;

    private World<AuthoritativePlayerController> world;
    private LocalPlayerController localPlayerController;

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
                    GameAuthoritativeListener playerController;
                    if (playerState.getId().equals(id.toString())) {
                        playerController = localPlayerController;
                    } else {
                        playerController = world.getPlayerController(playerState.getId());
                        //playerController.setNextFireBullet(playerState.getBullet().getFired());
                    }
                    playerController.setNextState(new Player(
                            new Point2D(playerState.getPositionX(), playerState.getPositionY()),
                            playerState.getRotation()
                    ));
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

    // NOTE: probably just single PlayerInput variable will be sufficient
    private final Queue<PlayerInput> inputQueue = new ArrayDeque<>();

    @Override
    public void notifyInput(PlayerInput playerInput) {
        inputQueue.add(playerInput);
    }

    @Override
    public void syncState() {
        while (!inputQueue.isEmpty()) {
            PlayerInput playerInput = inputQueue.poll();
            GameStateRequest request = GameStateRequest.newBuilder()
                    .setPlayer(LocalPlayerInput.newBuilder()
                            .setId(id.toString())
                            .setDelta(playerInput.getDelta())
                            .setInputX(playerInput.getX())
                            .setInputY(playerInput.getY())
                            .setShootPressed(playerInput.isShootPressed())
                            .build())
                    .build();

            gameStateRequestStream.onNext(request);
        }
    }

    @Override
    public void enterGame(LocalPlayerController localPlayerController, World<AuthoritativePlayerController> world) {
        this.localPlayerController = localPlayerController;
        this.world = world;
    }
}
