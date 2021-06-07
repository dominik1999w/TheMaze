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

    private ScreenAdapter screen;

    @Override
    public void create() {
        screen = new DebugScreen(this);
        setScreen(screen);
    }


    @Override
    public void dispose() {
        screen.dispose();
    }

}
