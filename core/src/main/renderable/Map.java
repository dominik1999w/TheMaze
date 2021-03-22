package renderable;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;

import java.util.List;

import map.config.MapConfig;
import map.containers.MapTile;
import map.containers.MapWall;

public class Map implements Renderable {

    private final OrthogonalTiledMapRenderer render;

    public Map(List<MapTile> map) {
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
            tileLayer.setCell((int) tile.getPosition().x, (int) tile.getPosition().y, tileCell);

            for (MapWall wall : tile.getWalls()) {
                TiledMapTileLayer.Cell wallCell = new TiledMapTileLayer.Cell();
                wallCell.setTile(new StaticTiledMapTile(wall.getTextureRegion()));

                if (wall.getTextureRegion().getRegionHeight() < wall.getTextureRegion().getRegionWidth()) {
                    horizontalWallLayer.setCell((int) wall.getPosition().x, (int) wall.getPosition().y, wallCell);
                } else {
                    verticalWallLayer.setCell((int) wall.getPosition().x, (int) wall.getPosition().y, wallCell);
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

