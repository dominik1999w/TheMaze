package world;

import java.util.UUID;

public class RoundResult {
    public UUID shooter;
    public UUID killed;
    public Integer shooterPoints;
    public Integer killedPoints;

    RoundResult(UUID shooter) {
        this.shooter = shooter;
        this.shooterPoints = -1;
    }

    RoundResult(UUID shooter, UUID killed) {
        this.shooter = shooter;
        this.shooterPoints = 3;
        this.killed = killed;
        this.killedPoints = -1;
    }

    public Integer getShooterPoints() {
        return shooterPoints;
    }
    public Integer getKilledPoints() {
        return killedPoints;
    }
}
