package ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import java.util.Map;
import java.util.UUID;

import entity.player.PlayerInput;
import types.SkinType;

public class GameUI {

    private final Stage stage;
    private final Table table;
    private final Skin skin;

    private Label points;
    private Label countdown;
    private Touchpad movementTouchpad;
    private CheckBox micButton;
    private VerticalGroup activePlayerMics;

    private boolean shootButtonPressed = false;

    public GameUI(AssetManager assetManager) {
        this.stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        this.table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        //table.setDebug(true);
        skin = assetManager.get(SkinType.ATTACK.getName());
    }

    public void build() {
        table.row().colspan(2);
        this.points = new Label("", skin, "title");
        updatePoints(0);
        table.add(points).left().top().padTop(50).padLeft(50).padRight(50);

        this.activePlayerMics = new VerticalGroup();
        table.add(activePlayerMics).right().top().padTop(50).padRight(50).padLeft(50).fillY();

        table.row().colspan(2);
        countdown = new Label("", skin, "title-bg");
        countdown.setAlignment(Align.center);
        updateCountdown(3);
        table.add(countdown).center().padLeft(50).padRight(50).expand();

        table.row();
        movementTouchpad = new Touchpad(20, skin);
        table.add(movementTouchpad).width(400).height(400).left().bottom().padLeft(50).padBottom(50);

        this.micButton = new CheckBox("MIC", skin, "mic");
        table.add(micButton).width(100).height(100).right().bottom().padRight(100).padBottom(100);

        ImageTextButton shootButton = new ImageTextButton(null, skin, "shoot");
        table.add(shootButton).width(200).height(200).right().bottom().padRight(100).padBottom(100);

        shootButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (super.touchDown(event, x, y, pointer, button)) {
                    shootButtonPressed = true;
                    return true;
                } else return false;
            }
        });
    }

    public PlayerInput readInput() {
        try {
            return new PlayerInput(movementTouchpad.getKnobPercentX(), movementTouchpad.getKnobPercentY(), shootButtonPressed);
        } finally {
            shootButtonPressed = false;
        }
    }

    public boolean isMicActive() {
        return micButton.isChecked();
    }

    public void updatePoints(int points) {
        this.points.setText("Points: " + points);
    }

    public void updateCountdown(int time) {
        countdown.setVisible(time > 0);
        if (time > 0) {
            String newText = "New round in:\n" + time + " second" + (time > 1 ? "s" : "");
            countdown.setText(newText);
        }
    }

    public void updateActivePlayerMics(Map<String, String> clientsNames, Iterable<UUID> activeMics) {
        activePlayerMics.clear();
        for (UUID activeMic : activeMics) {
            activePlayerMics.addActor(new Label(clientsNames.get(activeMic.toString()), skin));
        }
        activePlayerMics.layout();
    }

    public void render(float delta, boolean resetInput) {
        stage.act(delta);
        stage.draw();

        if (resetInput) {
            shootButtonPressed = false;
        }
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }
}
