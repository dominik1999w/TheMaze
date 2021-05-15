package game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.UUID;

import io.grpc.netty.shaded.io.netty.util.internal.logging.InternalLoggerFactory;
import io.grpc.netty.shaded.io.netty.util.internal.logging.JdkLoggerFactory;
import types.SkinType;
import types.TextureType;

public class GameApp extends Game {
    private SpriteBatch batch;
    private ScreenAdapter screen;
    private AssetManager assetManager;

    @Override
    public void create() {
        InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE);

        UUID playerID = UUID.randomUUID();
        batch = new SpriteBatch();
        assetManager = new AssetManager();
        for (TextureType textureType : TextureType.values()) {
            assetManager.load(textureType.getName(), Texture.class);
        }
        for (SkinType skinType : SkinType.values()) {
            assetManager.load(skinType.getName(), Skin.class);
        }

        assetManager.finishLoading();

        screen = new MenuScreen(playerID, this, batch, assetManager);

        setScreen(screen);
    }


    @Override
    public void dispose() {
        batch.dispose();
        screen.dispose();
        assetManager.dispose();
    }
}