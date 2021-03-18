package renderable;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;

import java.util.List;

public class TileMap implements Renderable {
    private final OrthogonalTiledMapRenderer render;

    public TileMap(List<Tile> map) {
        render = new OrthogonalTiledMapRenderer(convertTileMapGdxMap(map));
    }

    private TiledMap convertTileMapGdxMap(List<Tile> map) {
        TiledMap tiledMap = new TiledMap();
        int dim = (int) Math.sqrt(map.size());
        TiledMapTileLayer layer = new TiledMapTileLayer(dim, dim, Tile.width, Tile.height);
        for (Tile tile : map) {
            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(new StaticTiledMapTile(tile.getTextureRegion()));
            layer.setCell((int) tile.getPosition().x, (int) tile.getPosition().y, cell);
        }
        tiledMap.getLayers().add(layer);
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

