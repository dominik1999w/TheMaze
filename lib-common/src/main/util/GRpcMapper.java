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

    public static Player playerState(PlayerState message) {
        return new Player(UUID.fromString(message.getId()),
                new Point2D(message.getPositionX(), message.getPositionY()),
                message.getRotation()
        );
    }

    public static Bullet bulletState(BulletState message) {
        return new Bullet(
                UUID.fromString(message.getId()),
                new Point2D(message.getPositionX(), message.getPositionY()),
                message.getRotation()
        );
    }

    private GRpcMapper() {}
}
