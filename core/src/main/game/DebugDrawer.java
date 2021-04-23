package game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import entity.player.Player;
import map.Map;
import map.MapConfig;
import util.Point2D;
import util.Point2Di;

import static util.MathUtils.floor;

final class DebugDrawer {

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    private final Camera camera;
    private final Map map;
    private final Player player;
    private final Point2D previousPlayerPosition = new Point2D();

    DebugDrawer(Camera camera, Map map, Player player) {
        this.camera = camera;
        this.map = map;
        this.player = player;
        this.previousPlayerPosition.set(player.getPosition());

        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.setAutoShapeType(false);
    }

    void draw() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setColor(0, 1, 0, 0.5f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        Point2D currentPosition = new Point2D(previousPlayerPosition);
        Point2D targetPosition = new Point2D(player.getPosition());

        Point2Di currentTile = new Point2Di(
                floor(currentPosition.x() / MapConfig.BOX_SIZE),
                floor(currentPosition.y() / MapConfig.BOX_SIZE)
        );
        Point2Di targetTile = new Point2Di(
                floor(targetPosition.x() / MapConfig.BOX_SIZE),
                floor(targetPosition.y() / MapConfig.BOX_SIZE)
        );

        Point2Di collisionAreaMin = new Point2Di(currentTile)
                .min(targetTile)
                .subtract(new Point2Di(1, 1))
                .max(new Point2Di(0, 0));
        Point2Di collisionAreaMax = new Point2Di(currentTile)
                .max(targetTile)
                .add(new Point2Di(2, 2))
                .min(new Point2Di(map.getMapLength(), map.getMapLength()));

        shapeRenderer.rect(collisionAreaMin.x() * MapConfig.BOX_SIZE,
                collisionAreaMin.y() * MapConfig.BOX_SIZE,
                (collisionAreaMax.x() - collisionAreaMin.x()) * MapConfig.BOX_SIZE,
                (collisionAreaMax.y() - collisionAreaMin.y()) * MapConfig.BOX_SIZE);

        /*for (WallType.WallShape wallShape : map.getWallsInArea(collisionAreaMin, collisionAreaMax)) {
            shapeRenderer.rect(
                    wallShape.getPositionX() * MapConfig.BOX_SIZE,
                    wallShape.getPositionY() * MapConfig.BOX_SIZE,
                    wallShape.getSizeX(), wallShape.getSizeY()
            );
        }*/

        shapeRenderer.end();

        previousPlayerPosition.set(player.getPosition());
    }
}
