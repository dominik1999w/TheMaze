package game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import types.SkinType;
import util.Menu;

public class ScoreScreen extends ScreenAdapter {
    private final Stage stage;
    private final Skin skin;
    GameApp game;
    AssetManager assetManager;
    private Container<Actor> menuContainer;

    public ScoreScreen(GameApp game, AssetManager assetManager, Map<String, Integer> points, boolean isHost) {
        this.game = game;
        this.assetManager = assetManager;
        this.skin = assetManager.get(SkinType.GLASSY.getName());
        this.stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        buildUI(points, isHost);
    }

    private void buildUI(Map<String, Integer> points, boolean isHost) {
        menuContainer = Menu.buildMenuContainer(getTextButtons(isHost), skin);
        Container<Actor> scoresContainer = buildScoresContainer(points);

        stage.addActor(menuContainer);
        stage.addActor(scoresContainer);

        Gdx.input.setInputProcessor(stage);
    }

    private Container<Actor> buildScoresContainer(Map<String, Integer> points) {
        Menu.Builder scoresBuilder = new Menu.Builder()
                .defaultSettings(menuContainer.getWidth(), menuContainer.getHeight(), menuContainer.getWidth(), (Gdx.graphics.getHeight() - menuContainer.getHeight()) / 2)
                .addTable();

        int nr_players = points.size();
        float fontScale = Math.min((float) 10 / nr_players, 1.5f);
        for (Map.Entry<String, Integer> entry : points.entrySet()) {
            scoresBuilder.addLabel(
                    entry.getKey() + ":  " + entry.getValue(),
                    skin,
                    "big",
                    fontScale
            ).addPadding(0.0f);
        }

        return scoresBuilder.build();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
    }


    private List<TextButton> getTextButtons(boolean isHost) {
        List<TextButton> buttons = new ArrayList<>();
        if (isHost) {
            buttons.add(
                    Menu.getTextButton("Start New Game", skin, new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            Gdx.app.exit();
                        }
                    })
            );
        }
        buttons.add(
                Menu.getTextButton("Back To Menu", skin, new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Gdx.app.exit();
                    }
                })
        );
        buttons.add(
                Menu.getTextButton("Quit", skin, new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Gdx.app.exit();
                    }
                })
        );
        return buttons;
    }
}
