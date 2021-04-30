package service;

import java.util.ArrayList;
import java.util.List;

import entity.player.controller.PlayerController;
import io.grpc.stub.StreamObserver;
import lib.connection.BulletState;
import lib.connection.GameStateResponse;
import lib.connection.PlayerState;
import world.World;

public class GameReplyService {

    private final List<StreamObserver<GameStateResponse>> responseObservers = new ArrayList<>();

    private final World<?> world;

    public GameReplyService(World<?> world) {
        this.world = world;
    }

    public void broadcastGameState() {
        GameStateResponse.Builder response = GameStateResponse.newBuilder();
        for (java.util.Map.Entry<String, ? extends PlayerController> connectedPlayer : world.getConnectedPlayers()) {
            response.addPlayers(PlayerState.newBuilder()
                    .setId(connectedPlayer.getKey())
                    .setPositionX(connectedPlayer.getValue().getPlayerPosition().x())
                    .setPositionY(connectedPlayer.getValue().getPlayerPosition().y())
                    .setRotation(connectedPlayer.getValue().getPlayerRotation())
                    .setBullet(BulletState.newBuilder()
                            .setFired(world.getBulletController(connectedPlayer.getValue().getPlayer()) != null)
                            .build())
                    .build());
        }
        responseObservers.forEach(observer -> observer.onNext(response.build()));
    }

    void addResponseObserver(StreamObserver<GameStateResponse> responseObserver) {
        responseObservers.add(responseObserver);
    }
}
