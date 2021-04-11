package renderable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;

import map.Map;
import map.config.MapConfig;
import types.TextureType;
import types.WallType;

public class MapView implements Renderable {

    private final AssetManager assetManager;
    private final MapRenderer render;

    public MapView(Map map, AssetManager assetManager) {
        this.assetManager = assetManager;
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
                TiledMapTileLayer.Cell tileCell = new TiledMapTileLayer.Cell();

                Texture tileTexture = assetManager.get(TextureType.GROUND.getName());
                TextureRegion tileTextureRegion = new TextureRegion(tileTexture, 0, 0, MapConfig.BOX_SIZE, MapConfig.BOX_SIZE);
                tileCell.setTile(new StaticTiledMapTile(tileTextureRegion));
                tileLayer.setCell(tiles[i][j].getPositionX(), tiles[i][j].getPositionY(), tileCell);

                Map.Node tile = tiles[i][j];
                for (WallType type : tile.getWallRelativePositions()) {
                    TiledMapTileLayer.Cell wallCell = new TiledMapTileLayer.Cell();

                    WallType.WallShape wallShape = type.getWallShape(
                            tile.getPositionX(), tile.getPositionY(),
                            MapConfig.BOX_SIZE, MapConfig.WALL_THICKNESS
                    );
                    Texture wallTexture = assetManager.get(TextureType.WALL.getName());
                    TextureRegion wallTextureRegion = new TextureRegion(wallTexture, 0, 0, wallShape.getSizeX(), wallShape.getSizeY());
                    wallCell.setTile(new StaticTiledMapTile(wallTextureRegion));

                    if (wallShape.getSizeY() < wallShape.getSizeX()) {
                        horizontalWallLayer.setCell(wallShape.getPositionX(), wallShape.getPositionY(), wallCell);
                    } else {
                        verticalWallLayer.setCell(wallShape.getPositionX(), wallShape.getPositionY(), wallCell);
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

