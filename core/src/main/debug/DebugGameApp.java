package debug;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import types.SkinType;
import types.TextureType;

public class DebugGameApp extends Game {

    private SpriteBatch batch;
    private ScreenAdapter screen;
    private AssetManager assetManager;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assetManager = new AssetManager();
        for (TextureType textureType : TextureType.values()) {
            assetManager.load(textureType.getName(), Texture.class);
        }
        for (SkinType skinType : SkinType.values()) {
            assetManager.load(skinType.getName(), Skin.class);
        }

        assetManager.finishLoading();

        screen = new DebugScreen(this, batch, assetManager);

        setScreen(screen);
    }


    @Override
    public void dispose() {
        batch.dispose();
        screen.dispose();
        assetManager.dispose();
    }

}
