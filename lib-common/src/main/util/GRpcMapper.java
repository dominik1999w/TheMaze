package util;

import entity.player.PlayerInput;
import lib.connection.LocalPlayerInput;

public final class GRpcMapper {

    public static PlayerInput playerInput(LocalPlayerInput message) {
        return new PlayerInput(
                message.getDelta(), message.getInputX(), message.getInputY(), message.getShootPressed()
        );
    }

    private GRpcMapper() {}
}
