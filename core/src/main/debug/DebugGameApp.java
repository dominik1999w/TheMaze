package debug;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import game.Permissions;
import types.SkinType;
import types.TextureType;

public class DebugGameApp extends Game {

    private ScreenAdapter screen;
    private final Permissions permissions;

    public DebugGameApp(Permissions permissions) {
        this.permissions = permissions;
    }

    @Override
    public void create() {
        if (!permissions.isAudioPermissionEnabled()) {
            permissions.requestAudioPermission();
        }

        screen = new DebugScreen(this);
        setScreen(screen);
    }


    @Override
    public void dispose() {
        screen.dispose();
    }
}
