package world;

import java.util.UUID;

import entity.bullet.BulletController;

public final class CachedBullet {

    private UUID shooterID;
    private BulletController bulletController;

    public void disable() {
        bulletController = null;
    }

    public boolean enabled() {
        return bulletController != null;
    }

    public void passTo(UUID shooterID) {
        this.shooterID = shooterID;
    }

    public void enable(BulletController bulletController) {
        this.bulletController = bulletController;
    }

    public UUID getShooterID() {
        return shooterID;
    }

    public UUID getID() {
        return bulletController.getBullet().getId();
    }

    public BulletController getController() {
        return bulletController;
    }
}
