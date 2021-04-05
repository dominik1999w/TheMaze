package game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import db.Database;
import filereader.FileEngine;
import io.grpc.netty.shaded.io.netty.util.internal.logging.InternalLoggerFactory;
import io.grpc.netty.shaded.io.netty.util.internal.logging.JdkLoggerFactory;
import loader.GameLoader;
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
        Map map = new Map(mapGenerator.generateMap());
        db.saveMap(map);
        GameLoader loader = new GameLoader(db);
        batch = new SpriteBatch();
        gameScreen = new OnlineGameScreen(batch, loader, map);
        setScreen(gameScreen);
    }

    @Override
    public void dispose() {
        batch.dispose();
        gameScreen.dispose();
    }
}