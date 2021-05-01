package service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import entity.player.controller.PlayerController;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lib.connection.BulletState;
import lib.connection.GameStateResponse;
import lib.connection.PlayerState;
import world.World;

public class GameReplyService {

    private final Map<String, StreamObserver<GameStateResponse>> responseObservers = new HashMap<>();

    private final World<?> world;

    private final Map<String, Long> lastProcessedInput = new ConcurrentHashMap<>();

    public GameReplyService(World<?> world) {
        this.world = world;
    }

    public void broadcastGameState() {
        GameStateResponse.Builder response = GameStateResponse.newBuilder();
        for (java.util.Map.Entry<String, ? extends PlayerController> connectedPlayer : world.getConnectedPlayers()) {
            String id = connectedPlayer.getKey();
            PlayerController controller = connectedPlayer.getValue();
            System.out.println("Acknowledging input " + lastProcessedInput.getOrDefault(id, 0L));
            response.addPlayers(PlayerState.newBuilder()
                    .setSequenceNumber(lastProcessedInput.getOrDefault(id, 0L))
                    .setId(id)
                    .setPositionX(controller.getPlayerPosition().x())
                    .setPositionY(controller.getPlayerPosition().y())
                    .setRotation(controller.getPlayerRotation())
                    .setBullet(BulletState.newBuilder()
                            .setFired(world.getBulletController(controller.getPlayer()) != null)
                            .build())
                    .build());
        }
        GameStateResponse stateResponse = response.build();

        Iterator<Map.Entry<String, StreamObserver<GameStateResponse>>> iterator = responseObservers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, StreamObserver<GameStateResponse>> entry = iterator.next();
            StreamObserver<GameStateResponse> responseObserver = entry.getValue();
            try {
                responseObserver.onNext(stateResponse);
            } catch (StatusRuntimeException e) {
                System.err.println(String.format(Locale.ENGLISH,
                        "Player %s disconnected", entry.getKey()));
                iterator.remove();
                // remove player from world
            }
        }
    }

    void onInputProcessed(String playerID, long sequenceNumber) {
        Long oldSequenceNumber = lastProcessedInput.get(playerID);
        if (oldSequenceNumber == null || oldSequenceNumber < sequenceNumber)
            lastProcessedInput.put(playerID, sequenceNumber);
    }

    void addResponseObserver(String id, StreamObserver<GameStateResponse> responseObserver) {
        responseObservers.putIfAbsent(id, responseObserver);
    }
}
