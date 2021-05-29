package world;

import java.util.UUID;

public class RoundResult {
    public UUID shooter;
    public UUID killed;
    public Integer shooterPoints;
    public Integer killedPoints;

    public RoundResult(UUID shooter) {
        this.shooter = shooter;
        this.shooterPoints = -1;
    }

    public RoundResult(UUID shooter, UUID killed) {
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

    @Override
    public String toString() {
        String s = "-- Round results:\n" +
                "Shooter: " + shooter + " = " + shooterPoints;
        if(killed != null) s += "\n" + "Killed: " + killed + " = " + killedPoints;
        return s;
    }
}
