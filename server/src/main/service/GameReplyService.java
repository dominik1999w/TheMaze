package service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

    private final List<ResponseObserver> responseObservers = new ArrayList<>();

    private final World<?> world;

    private final Map<String, Long> lastProcessedInput = new ConcurrentHashMap<>();

    public GameReplyService(World<?> world) {
        this.world = world;
    }

    public void broadcastGameState() {
        GameStateResponse.Builder response = GameStateResponse.newBuilder();
        for (java.util.Map.Entry<String, ? extends PlayerController> connectedPlayer : world.getConnectedPlayers()) {
            response.addPlayers(PlayerState.newBuilder()
                    .setSequenceNumber(lastProcessedInput.getOrDefault(connectedPlayer.getKey(), 0L))
                    .setId(connectedPlayer.getKey())
                    .setPositionX(connectedPlayer.getValue().getPlayerPosition().x())
                    .setPositionY(connectedPlayer.getValue().getPlayerPosition().y())
                    .setRotation(connectedPlayer.getValue().getPlayerRotation())
                    .setBullet(BulletState.newBuilder()
                            .setFired(world.getBulletController(connectedPlayer.getValue().getPlayer()) != null)
                            .build())
                    .build());
        }
        GameStateResponse stateResponse = response.build();

        Iterator<ResponseObserver> iterator = responseObservers.iterator();
        while (iterator.hasNext()) {
            ResponseObserver responseObserver = iterator.next();
            try {
                responseObserver.observer.onNext(stateResponse);
            } catch (StatusRuntimeException e) {
                System.err.println(String.format(Locale.ENGLISH,
                        "Player %s disconnected", responseObserver.id));
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

    // NOTE: maybe merge these 2 methods in a single call?
    void addResponseObserver(StreamObserver<GameStateResponse> responseObserver) {
        responseObservers.add(new ResponseObserver(responseObserver));
    }

    void associateResponseObserverWith(StreamObserver<GameStateResponse> observer, String id) {
        responseObservers.stream()
                .filter(responseObserver -> responseObserver.observer == observer)
                .findAny()
                .ifPresent(responseObserver -> responseObserver.id = id);
    }

    private static class ResponseObserver {
        private final StreamObserver<GameStateResponse> observer;
        private String id;
        public ResponseObserver(StreamObserver<GameStateResponse> observer) {
            this.observer = observer;
        }
    }
}
