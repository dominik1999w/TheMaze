package game;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import entity.player.controller.InputPlayerController;
import lib.map.Position;
import service.GameService;
import service.MapService;
import service.StateService;
import time.CustomTimer;
import world.RoundResult;

import static util.ServerConfig.SERVER_UPDATE_RATE;

public class Game {
    private static final Logger logger = Logger.getLogger(Game.class.getName());

    private final GameService gameService;
    private final MapService mapService;
    private final StateService stateService;

    private Map<UUID, Position> initialPositions = new HashMap<>();
    private final Map<String, Integer> points = new HashMap<>();
    private final AtomicBoolean newRoundStarted = new AtomicBoolean(false);

    private CustomTimer gameTask = new CustomTimer();

    public Game(MapService mapService, StateService stateService, GameService gameService) {
        this.mapService = mapService;
        this.stateService = stateService;
        this.gameService = gameService;
    }

    public void startGame() {
        new Thread(() -> {
            CountDownLatch latch = new CountDownLatch(1);
            new Timer().scheduleAtFixedRate(getMapTask(latch), 0, 1000L);

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    endGame();
                }
            }, 1 * 60 * 1000);

            for (String name : mapService.getNames().values()) {
                points.put(name, 0);
            }

            new Thread(this::getGameTask).start();

            startNewRound();
        }).start();

    }

    private void getGameTask() {

        gameTask = new CustomTimer();
        gameTask.executeAtFixedRate(delta -> {
            if (newRoundStarted.get()) {
                gameService.startNewRound(initialPositions);
                newRoundStarted.set(false);
            }

            gameService.dispatchMessages((sequenceNumber, timestamp, id, playerInput) ->
            {
                InputPlayerController playerController = gameService.getWorld().getPlayerController(id);
                playerController.updateInput(playerInput);
                playerController.update();
                gameService.getCollisionWorld().onPlayerMoved(playerController.getPlayer().getId(), timestamp, playerInput.getDelta());
            });
            gameService.getWorld().update(delta);
            gameService.getCollisionWorld().update();
            gameService.broadcastGameState(System.currentTimeMillis());
        }, 1.0f / SERVER_UPDATE_RATE);
    }

    public void startNewRound() {
        logger.info("Starting new round...");
        CountDownLatch latch = new CountDownLatch(1);
        TimerTask task = getCountdownTask(latch);
        new Thread(() -> {
            new Timer().scheduleAtFixedRate(task, 0, 1000L);
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("new round settings have completed");
            newRoundStarted.set(true);
        }).start();

    }

    public void endRound(RoundResult result) {
        System.out.println(result);
        result.getPoints().forEach(
                (uuid, integer) -> points.merge(mapService.getNames().get(uuid), integer, Integer::sum));
        startNewRound();
    }

    public void endGame() {
        gameTask.cancel();
        stateService.broadcastState(0.0f, points, true);
    }

    private TimerTask getCountdownTask(CountDownLatch latch) {
        return new TimerTask() {
            private int remainingTime = 3;

            @Override
            public void run() {
                stateService.broadcastState(remainingTime, points, false);
                logger.info(String.valueOf(remainingTime));
                if (remainingTime == 0.0f) {
                    latch.countDown();
                    cancel();
                    return;
                }
                remainingTime--;
            }
        };
    }

    private TimerTask getMapTask(CountDownLatch latch) {
        return new TimerTask() {
            @Override
            public void run() {
                mapService.dispatchMessages();
                mapService.broadcastMapState((mapLength, seed, initialPositions) -> {
                    Game.this.initialPositions = initialPositions;
                    gameService.initializeWorld(Game.this, mapLength, seed, initialPositions); // required for world preview during pregame countdown
                    latch.countDown();
                    cancel();
                });
            }
        };
    }

}
