package ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;

import input.IPlayerInput;
import input.PlayerInput;

public class GameUI {

    private final Stage stage;
    private final Table table;
    private final Skin skin;
    private Touchpad movementTouchpad;
    private Button shootButton;

    private final PlayerInput playerInput = new PlayerInput();

    public GameUI() {
        this.stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        this.table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        table.setDebug(true);
        skin = new Skin(Gdx.files.internal("skins/uiskin.json"));
    }

    public void build() {
        movementTouchpad = new Touchpad(20, skin);
        table.add(movementTouchpad).width(400).height(400).expand().left().bottom().padLeft(50).padBottom(50);

        shootButton = new Button(skin);
        table.add(shootButton).width(200).height(200).expand().right().bottom().padRight(100).padBottom(100);
    }

    public void readInput() {
        playerInput.setX(convertKnobPercentageToDirection(movementTouchpad.getKnobPercentX()));
        playerInput.setY(convertKnobPercentageToDirection(movementTouchpad.getKnobPercentY()));
        playerInput.setShootPressed(shootButton.isPressed());
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

    public IPlayerInput getPlayerInput() {
        return playerInput;
    }

    // TODO: move to some utility class
    private static int convertKnobPercentageToDirection(float percentage) {
        int sign = (int)Math.signum(percentage);
        return Math.abs(percentage) > 0.5f ? sign : 0;
    }
}
