package ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;

import java.util.ArrayList;
import java.util.List;

import entity.player.GameInputListener;
import entity.player.PlayerInput;
import types.SkinType;

public class GameUI {

    private final Stage stage;
    private final Table table;
    private final Skin skin;
    private Touchpad movementTouchpad;
    private ImageTextButton shootButton;

    public GameUI(AssetManager assetManager) {
        this.stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        this.table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        table.setDebug(true);
        skin = assetManager.get(SkinType.ATTACK.getName());
    }

    public void build() {
        movementTouchpad = new Touchpad(20, skin);
        table.add(movementTouchpad).width(400).height(400).expand().left().bottom().padLeft(50).padBottom(50);

        shootButton = new ImageTextButton(null, skin);
        table.add(shootButton).width(200).height(200).expand().right().bottom().padRight(100).padBottom(100);
    }

    public PlayerInput readInput() {
        return new PlayerInput(movementTouchpad.getKnobPercentX(), movementTouchpad.getKnobPercentY(), shootButton.isPressed());
    }

    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }
}
