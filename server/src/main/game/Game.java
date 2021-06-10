package game;

import com.google.common.collect.Iterables;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import entity.player.controller.InputPlayerController;
import service.GameService;
import service.MapService;
import service.StateService;
import time.CustomTimer;
import world.RoundResult;

public class Game {
    private static final Logger logger = Logger.getLogger(Game.class.getName());

    private final int tickRate;

    private final GameService gameService;
    private final MapService mapService;
    private final StateService stateService;

    private final Map<String, Integer> points = new ConcurrentHashMap<>();
    private final AtomicBoolean newRoundStarted = new AtomicBoolean(false);
    private final AtomicBoolean gameOver = new AtomicBoolean(true);

    private CustomTimer gameTask = new CustomTimer();

    public Game(MapService mapService, StateService stateService, GameService gameService, int tickRate) {
        this.mapService = mapService;
        this.stateService = stateService;
        this.gameService = gameService;
        this.tickRate = tickRate;
    }

    public void startGame() {
        points.clear();
        mapService.prepareMapService();
        CountDownLatch latch = new CountDownLatch(1);
        new Timer().scheduleAtFixedRate(getMapTask(latch), 0, 1000L);

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (String name : mapService.getNames().values()) {
            points.put(name, 0);
        }

        gameOver.set(false);

        new Thread(this::getGameTask).start();

        startNewRound();
    }

    private void getGameTask() {
        gameTask = new CustomTimer();
        gameTask.executeAtFixedRate(delta -> {
            if (newRoundStarted.get()) {
                gameService.startNewRound(mapService.updateInitialPositions());
                newRoundStarted.set(false);
            }
            if (Iterables.isEmpty(gameService.getWorld().getConnectedPlayers())) {
                gameService.endGame();
                gameOver.set(true);
                endGame();
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
        }, 1.0f / tickRate);
    }

    public void startNewRound() {
        if (gameOver.get()) {
            return;
        }

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
        result.getPoints().forEach(((uuid, integer) -> {
            points.merge(mapService.getNames().get(uuid.toString()), integer, Integer::sum);
            if (points.get(mapService.getNames().get(uuid.toString())) >= 10) {
                gameOver.set(true);
            }
        }));

        if (gameOver.get()) {
            endGame();
        } else {
            startNewRound();
        }
    }

    public void endGame() {
        gameTask.cancel();
        stateService.broadcastState(0.0f, points, true);
        startGame();
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
                mapService.broadcastMapState((mapLength, seed, generatorType, initialPositions) -> {
                    gameService.initializeWorld(Game.this, mapLength, seed, generatorType, mapService.updateInitialPositions()); // required for world preview during pregame countdown
                    latch.countDown();
                    cancel();
                });
            }
        };
    }

}
