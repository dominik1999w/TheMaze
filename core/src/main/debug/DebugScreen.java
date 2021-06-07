package debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.UUID;

import entity.bullet.BulletController;
import entity.bullet.BulletHitbox;
import entity.player.Player;
import entity.player.PlayerHitbox;
import entity.player.PlayerInput;
import entity.player.controller.AuthoritativePlayerController;
import entity.player.controller.InputPlayerController;
import map.Map;
import map.MapConfig;
import map.generator.MapGenerator;
import physics.CollisionWorld;
import util.Point2D;
import world.World;

public class DebugScreen extends ScreenAdapter {
    private final DebugGameApp debugGameApp;

    public DebugScreen(DebugGameApp debugGameApp) {
        this.debugGameApp = debugGameApp;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.5f, .5f, .5f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
