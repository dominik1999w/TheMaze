package util;

import entity.player.controller.InputPlayerController;
import service.GameService;
import service.MapService;
import time.Timer;

import static util.ServerConfig.SERVER_UPDATE_RATE;

public class StateHandler {

    private final GameService gameService;
    private final MapService mapService;

    public StateHandler(MapService mapService, GameService gameService) {
        this.mapService = mapService;
        this.gameService = gameService;
    }

    public Thread mainThread() {
        return new Thread(() -> Timer.executeAtFixedRate(delta ->
        {
            if (!gameService.isEnabled()) {
                mapService.dispatchMessages();
                mapService.broadcastMapState((mapLength, seed, positions) -> {
                    gameService.initializeWorld(mapLength, seed, positions);
                    gameService.setEnabled(true);
                });

                return;
            }

            gameService.dispatchMessages((sequenceNumber, id, playerInput) ->
            {
                InputPlayerController playerController = gameService.getWorld().getPlayerController(id);
                playerController.updateInput(playerInput);
                playerController.update();
                gameService.getCollisionWorld().update();
            });
            // TODO: rewrite: in world.update only bullets will be actually updated
            gameService.getWorld().update(delta);
            gameService.getCollisionWorld().update();
            gameService.broadcastGameState(System.currentTimeMillis());
        }, 1.0f / SERVER_UPDATE_RATE));
    }

}
