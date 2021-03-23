package game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import db.Database;
import filereader.FileEngine;
import loader.GameLoader;
import map.generator.MapGenerator;

public class GameApp extends Game {
    private SpriteBatch batch;
    private GameScreen gameScreen;

    @Override
    public void create() {
        Database db = new Database(new FileEngine());
        MapGenerator mapGenerator = new MapGenerator();
        db.saveMap(mapGenerator.generateMap());
        GameLoader loader = new GameLoader(db);
        batch = new SpriteBatch();
        gameScreen = new GameScreen(batch, loader, mapGenerator);
        setScreen(gameScreen);
    }

    @Override
    public void dispose() {
    }
}