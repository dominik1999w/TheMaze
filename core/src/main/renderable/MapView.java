package renderable;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;

import map.Map;
import map.config.MapConfig;
import map.rendercontainers.MapTile;
import map.rendercontainers.MapWall;
import util.Point2D;
import util.Shape2D;

public class MapView implements Renderable {

    private final Map map;
    private final OrthogonalTiledMapRenderer render;

    public MapView(Map map) {
        this.map = map;
        render = new OrthogonalTiledMapRenderer(convertTileMapGdxMap(map));
    }

    private TiledMap convertTileMapGdxMap(Map map) {
        Map.Node[][] tiles = map.getMapArray();
        TiledMap tiledMap = new TiledMap();

        TiledMapTileLayer tileLayer =
                new TiledMapTileLayer(MapConfig.MAP_LENGTH + 1, MapConfig.MAP_LENGTH + 1, MapConfig.BOX_SIZE, MapConfig.BOX_SIZE);
        TiledMapTileLayer horizontalWallLayer =
                new TiledMapTileLayer(MapConfig.MAP_LENGTH + 1, MapConfig.MAP_LENGTH + 1, MapConfig.BOX_SIZE, MapConfig.BOX_SIZE);
        TiledMapTileLayer verticalWallLayer =
                new TiledMapTileLayer(MapConfig.MAP_LENGTH + 1, MapConfig.MAP_LENGTH + 1, MapConfig.BOX_SIZE, MapConfig.BOX_SIZE);

        for (int i = 0; i < MapConfig.MAP_LENGTH; i++) {
            for (int j = 0; j < MapConfig.MAP_LENGTH; j++) {
                Point2D position = new Point2D(tiles[i][j].getPositionX(), tiles[i][j].getPositionY());
                Point2D size = new Point2D(MapConfig.BOX_SIZE, MapConfig.BOX_SIZE);

                MapTile tile = new MapTile(new Shape2D(position, size), tiles[i][j].getWallRelativePositions());
                TiledMapTileLayer.Cell tileCell = new TiledMapTileLayer.Cell();
                tileCell.setTile(new StaticTiledMapTile(tile.getTextureRegion()));
                tileLayer.setCell((int) tile.getShape().getPosition().x, (int) tile.getShape().getPosition().y, tileCell);

                for (MapWall wall : tile.getWalls()) {
                    TiledMapTileLayer.Cell wallCell = new TiledMapTileLayer.Cell();
                    wallCell.setTile(new StaticTiledMapTile(wall.getTextureRegion()));

                    if (wall.getTextureRegion().getRegionHeight() < wall.getTextureRegion().getRegionWidth()) {
                        horizontalWallLayer.setCell((int) wall.getShape().getPosition().x, (int) wall.getShape().getPosition().y, wallCell);
                    } else {
                        verticalWallLayer.setCell((int) wall.getShape().getPosition().x, (int) wall.getShape().getPosition().y, wallCell);
                    }
                }
            }
        }

        tiledMap.getLayers().add(tileLayer);
        tiledMap.getLayers().add(horizontalWallLayer);
        tiledMap.getLayers().add(verticalWallLayer);

        return tiledMap;
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        render.render();
    }

    public void setView(OrthographicCamera camera) {
        render.setView(camera);
    }
}

