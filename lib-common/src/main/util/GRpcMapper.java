package util;

import java.util.UUID;

import entity.bullet.Bullet;
import entity.player.Player;
import entity.player.PlayerInput;
import lib.connection.BulletState;
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

    public static Bullet bulletState(BulletState bulletState) {
        return new Bullet(
                UUID.fromString(bulletState.getId()),
                new Point2D(bulletState.getPositionX(), bulletState.getPositionY()),
                bulletState.getRotation()
        );
    }

    private GRpcMapper() {}
}
