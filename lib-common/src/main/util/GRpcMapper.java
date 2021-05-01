package util;

import java.util.UUID;

import entity.player.Player;
import entity.player.PlayerInput;
import lib.connection.LocalPlayerInput;
import lib.connection.PlayerState;

public final class GRpcMapper {

    public static PlayerInput playerInput(LocalPlayerInput message) {
        return new PlayerInput(
                message.getDelta(), message.getInputX(), message.getInputY(), message.getShootPressed()
        );
    }

    public static Player playerState(PlayerState playerState) {
        return new Player(UUID.fromString(playerState.getId()),
                new Point2D(playerState.getPositionX(), playerState.getPositionY()),
                playerState.getRotation()
        );
    }

    private GRpcMapper() {}
}
