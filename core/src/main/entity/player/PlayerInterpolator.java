package entity.player;

import java.util.ArrayList;
import java.util.List;

import util.Point2D;

import static util.Interpolation.interpolate;

public class PlayerInterpolator {

    private final List<PlayerStateTimeStamp> playerStateHistory = new ArrayList<>();

    private final float serverDeltaMillis;

    public PlayerInterpolator(int serverUpdateRate) {
        this.serverDeltaMillis = 1000.0f / serverUpdateRate;
    }

    public void addState(Player state) {
        playerStateHistory.add(new PlayerStateTimeStamp(state, System.currentTimeMillis()));
    }

    public void computeCurrentState(Player out) {
        float renderTimestamp = System.currentTimeMillis() - serverDeltaMillis;

        int lastStateIndex = 0;
        while (lastStateIndex < playerStateHistory.size() &&
                playerStateHistory.get(lastStateIndex).timestamp <= renderTimestamp)
            lastStateIndex++;

        System.out.println(renderTimestamp + " " + lastStateIndex + "/" + playerStateHistory.size());

        if (lastStateIndex < playerStateHistory.size()) {
            if (lastStateIndex > 0) {
                // now stateHistory[lastStateIndex - 1] <= renderTimestamp <= stateHistory[lastStateIndex]
                PlayerStateTimeStamp playerStateTimeStampA = playerStateHistory.get(lastStateIndex - 1);
                PlayerStateTimeStamp playerStateTimeStampB = playerStateHistory.get(lastStateIndex);
                long timestampA = playerStateTimeStampA.timestamp;
                long timestampB = playerStateTimeStampB.timestamp;
                Point2D positionA = playerStateTimeStampA.playerState.getPosition();
                Point2D positionB = playerStateTimeStampB.playerState.getPosition();
                float rotationA = playerStateTimeStampA.playerState.getRotation();
                float rotationB = playerStateTimeStampB.playerState.getRotation();
                out.setPosition(interpolate(positionA, positionB, (renderTimestamp - timestampA) / (timestampB - timestampA)));
                out.setRotation(interpolate(rotationA, rotationB, (renderTimestamp - timestampA) / (timestampB - timestampA)));
            } else {
                out.setPosition(playerStateHistory.get(0).playerState.getPosition());
                out.setRotation(playerStateHistory.get(0).playerState.getRotation());
            }
        }

        while (lastStateIndex > 1) {
            playerStateHistory.remove(0);
            lastStateIndex--;
        }
    }

    private static class PlayerStateTimeStamp {
        private final Player playerState;
        private final long timestamp;
        private PlayerStateTimeStamp(Player playerState, long timestamp) {
            this.playerState = playerState;
            this.timestamp = timestamp;
        }
    }
}
