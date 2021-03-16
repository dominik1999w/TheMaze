package renderable;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

public class TileMap implements RenderEngine {
    private final ArrayList<Tile> map;

    public TileMap(ArrayList<Tile> map) {
        this.map = map;
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        for (Tile tile : map) {
            tile.render(spriteBatch);
        }
    }
}
