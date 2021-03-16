package game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import db.Database;
import filereader.FileEngine;
import loader.GameLoader;

public class GameApp extends Game {
    private SpriteBatch batch;
    private GameScreen gameScreen;

    @Override
    public void create() {
        Database db = new Database(new FileEngine());
        GameLoader loader = new GameLoader(db);
        batch = new SpriteBatch();
        gameScreen = new GameScreen(batch, loader);
        setScreen(gameScreen);
    }

    @Override
    public void dispose() {
    }
}