package renderable;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

public class TileMap implements Renderable {
    private final List<Tile> map;

    public TileMap(List<Tile> map) {
        this.map = map;
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        for (Tile tile : map) {
            tile.render(spriteBatch);
        }
    }
}
