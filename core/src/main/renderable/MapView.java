package renderable;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;

import java.util.List;

import map.config.MapConfig;
import map.rendercontainers.MapTile;
import map.rendercontainers.MapWall;

public class MapView implements Renderable {

    private final OrthogonalTiledMapRenderer render;

    public MapView(List<MapTile> map) {
        render = new OrthogonalTiledMapRenderer(convertTileMapGdxMap(map));
    }

    private TiledMap convertTileMapGdxMap(List<MapTile> map) {
        TiledMap tiledMap = new TiledMap();

        TiledMapTileLayer tileLayer =
                new TiledMapTileLayer(MapConfig.MAP_LENGTH+1, MapConfig.MAP_LENGTH+1, MapConfig.BOX_SIZE, MapConfig.BOX_SIZE);
        TiledMapTileLayer horizontalWallLayer =
                new TiledMapTileLayer(MapConfig.MAP_LENGTH+1, MapConfig.MAP_LENGTH+1, MapConfig.BOX_SIZE, MapConfig.BOX_SIZE);
        TiledMapTileLayer verticalWallLayer =
                new TiledMapTileLayer(MapConfig.MAP_LENGTH+1, MapConfig.MAP_LENGTH+1, MapConfig.BOX_SIZE, MapConfig.BOX_SIZE);

        for (MapTile tile : map) {
            TiledMapTileLayer.Cell tileCell = new TiledMapTileLayer.Cell();
            tileCell.setTile(new StaticTiledMapTile(tile.getTextureRegion()));
            tileLayer.setCell(tile.getPositionX(), tile.getPositionY(), tileCell);

            for (MapWall wall : tile.getWalls()) {
                TiledMapTileLayer.Cell wallCell = new TiledMapTileLayer.Cell();
                wallCell.setTile(new StaticTiledMapTile(wall.getTextureRegion()));

                if (wall.getTextureRegion().getRegionHeight() < wall.getTextureRegion().getRegionWidth()) {
                    horizontalWallLayer.setCell(wall.getPositionX(), wall.getPositionY(), wallCell);
                } else {
                    verticalWallLayer.setCell(wall.getPositionX(), wall.getPositionY(), wallCell);
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

