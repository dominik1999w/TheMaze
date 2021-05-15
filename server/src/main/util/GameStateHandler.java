package util;

import entity.player.controller.InputPlayerController;
import service.GameService;
import time.Timer;

import static util.ServerConfig.SERVER_UPDATE_RATE;

public class GameStateHandler {

    private final GameService gameService;

    public GameStateHandler(GameService gameService) {
        this.gameService = gameService;
    }


    public Thread gameThread() {
        return new Thread(() -> Timer.executeAtFixedRate(delta ->
        {
            if (!gameService.isEnabled()) {
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
