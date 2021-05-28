package physics.entitycollision;

import map.MapConfig;
import physics.HitboxHistory;
import util.Point2D;

public class SimpleEntityCollisionDetector implements EntityCollisionDetector {
    @Override
    public boolean detectCollision(HitboxHistory historyA, Point2D currentPositionB, Point2D targetPositionB, float radiusB) {
        Point2D currentPositionA = new Point2D(historyA.getPreviousPosition()).divide(MapConfig.BOX_SIZE);
        Point2D targetPositionA = new Point2D(historyA.getHitbox().getPosition()).divide(MapConfig.BOX_SIZE);
        currentPositionB = new Point2D(currentPositionB).divide(MapConfig.BOX_SIZE);
        targetPositionB = new Point2D(targetPositionB).divide(MapConfig.BOX_SIZE);

        float radiusA = historyA.getHitbox().getRadius();

        // dist(t) = mag(posB(t) - posA(t)) - radA - radB
        // posA'(t) = posA(t0) + (posA(t1) - posA(t0)) * t
        // posB'(t) = posB(t0) + (posB(t1) - posB(t0)) * t
        // dist'(t) = mag(posB'(t) - posA'(t)) - radA - radB = 0
        // sqrt((xB'(t) - xA'(t))^2 + (yB'(t) - yA'(t))^2) = (radA + radB)
        // (xB'(t) - xA'(t))^2 + (yB'(t) - yA'(t))^2 = (radA + radB)^2
        // (xB(t0) - xA(t0) + t * (xB(t1) - xB(t0) - xA(t1) + xA(t0)))^2 + (yB(t0) - yA(t0) + t * (yB(t1) - yB(t0) - yA(t1) + yA(t0)))^2 = (radA + radB)^2
        // deltaCurrent = (xB(t0) - xA(t0), yB(t0) - yA(t0)) = currentPositionB - currentPositionA
        // deltaTarget = (xB(t1) - xA(t1), yB(t1) - yA(t1)) = targetPositionB - targetPositionA
        // (deltaCurrentX + t * (deltaTargetX - deltaCurrentX))^2 + (deltaCurrentY + t * (deltaTargetY - deltaCurrentY))^2 = (radA + radB)^2
        // dcX^2 + 2t*(dtX - dcX) + t^2 * (dtX - dcX)^2 + dcY^2 + 2t*(dtY - dcY) + t^2 * (dtY - dcY)^2 = (radA + radB)^2
        // (dcX^2 + dcY^2) + 2t * ((dtX + dtY) - (dcX + dcY)) + t^2 * ((dtX - dcX)^2 + (dtY - dcY)^2) = (radA + radB)^2
        // deltaCurrent.mag + 2t * (ddX + ddY) + t^2 * (dd.mag) = (radA + radB)^2
        // t^2 * (dd.mag) + t * (2ddX + 2ddY) + (dc.mag - (radA + radB)^2) = 0
        // D = (2ddX + 2ddY)^2 - 4 * dd.mag * (dc.mag - (radA + radB)^2)
        // t = (-(ddX + ddY) +- sqrt(D)) / dd.mag

        float sumRadiusSq = (radiusA + radiusB) * (radiusA + radiusB);
        Point2D deltaCurrent = currentPositionB.subtract(currentPositionA);
        Point2D deltaTarget = targetPositionB.subtract(targetPositionA);
        Point2D deltaDelta = deltaTarget.subtract(deltaCurrent);
        float deltaDeltaMag = deltaDelta.mag();
        float deltaDeltaSum = deltaDelta.x() + deltaDelta.y();

        float quadraticDiscriminant = 4 * (
                deltaDeltaSum * deltaDeltaSum - deltaDeltaMag * (deltaCurrent.mag() - sumRadiusSq)
        );

        if (quadraticDiscriminant < 0f) return false;

        float sqrtDiscriminant = (float) Math.sqrt(quadraticDiscriminant);
        float collisionTimeL = (-deltaDeltaSum - sqrtDiscriminant) / deltaDeltaMag;
        float collisionTimeR = (-deltaDeltaSum + sqrtDiscriminant) / deltaDeltaMag;

        if (0 <= collisionTimeL && collisionTimeL <= 1) return true;
        if (0 <= collisionTimeR && collisionTimeR <= 1) return true;
        return false;
    }
}
