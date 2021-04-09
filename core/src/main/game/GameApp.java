package game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import db.Database;
import filereader.FileEngine;
import io.grpc.netty.shaded.io.netty.util.internal.logging.InternalLoggerFactory;
import io.grpc.netty.shaded.io.netty.util.internal.logging.JdkLoggerFactory;
import map.Map;
import map.generator.MapGenerator;

public class GameApp extends Game {
    private SpriteBatch batch;
    private ScreenAdapter gameScreen;

    @Override
    public void create() {
        InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE);
        Database db = new Database(new FileEngine());
        MapGenerator mapGenerator = new MapGenerator();
        Map map = mapGenerator.generateMap();
        db.saveMap(map);
        batch = new SpriteBatch();
        gameScreen = new OnlineGameScreen(batch, map);
        setScreen(gameScreen);
    }

    @Override
    public void dispose() {
        batch.dispose();
        gameScreen.dispose();
    }
}