package game;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Logger;

import entity.player.controller.InputPlayerController;
import lib.map.Position;
import service.GameService;
import service.MapService;
import service.StateService;
import time.Timer;
import world.RoundResult;

import static util.ServerConfig.SERVER_UPDATE_RATE;

public class Game {
    private static final Logger logger = Logger.getLogger(Game.class.getName());

    private final GameService gameService;
    private final MapService mapService;
    private final StateService stateService;

    private Timer stateTimer = new Timer();
    private Timer gameTimer = new Timer();

    private int mapLength = 5;
    private int seed = 0;
    private Map<UUID, Position> initialPositions = new HashMap<>();
    private Map<UUID, Integer> points = new HashMap<>();
    private Thread gameThread;

    public Game(MapService mapService, StateService stateService, GameService gameService) {
        this.mapService = mapService;
        this.stateService = stateService;
        this.gameService = gameService;
    }

    public void startGame() {
        new Thread(() -> {
            Timer timer = new Timer();
            timer.executeAtFixedRate(delta -> {
                mapService.dispatchMessages();
                mapService.broadcastMapState((mapLength, seed, initialPositions) -> {
                    this.mapLength = mapLength;
                    this.seed = seed;
                    this.initialPositions = initialPositions;
                    gameService.initializeWorld(this, mapLength, seed, initialPositions); // required for world preview during pregame countdown
                    timer.cancel();
                });
            }, 1.0f / SERVER_UPDATE_RATE);

            startNewRound();
        }).start();
    }

    public void startNewRound() {
        logger.info("Starting new round...");

        stateTimer = new Timer();
        gameTimer = new Timer();

        stateTimer.executeAtFixedRate(new Timer.TimedRunnable() {
            private int remainingTime = 3;

            @Override
            public void run(float delta) {
                if (remainingTime == 0.0f) {
                    stateTimer.cancel();
                    return;
                }
                logger.info(String.valueOf(remainingTime));
                stateService.broadcastPreRoundState(remainingTime, points);
                remainingTime -= delta;
            }
        }, 1.0f);

        logger.info("new round just started...");
        stateService.broadcastPreRoundState(-1, points);

        gameThread = new Thread(new TimerTask() {
            @Override
            public void run() {
                gameService.startNewRound(initialPositions);
                gameTimer.executeAtFixedRate(delta -> {
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
                }, 1.0f / SERVER_UPDATE_RATE);
            }
        });
        gameThread.start();

    }

    public void endRound(RoundResult result) {
//        stateTimer.cancel();
//        gameTimer.cancel();
        gameTimer.cancel();
        gameThread.interrupt();

        System.out.println(result.shooter + " -- " + result.getShooterPoints());
        points.put(result.shooter, points.getOrDefault(result.shooter, 0) + result.getShooterPoints());
        if (result.killed != null) {
            points.put(result.killed, points.getOrDefault(result.killed, 0) + result.getKilledPoints());
        }

        startNewRound();
    }

}
