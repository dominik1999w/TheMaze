package util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.List;


public class Menu {
    public final static int defaultHeight = 1080;

    public static class Builder {
        private Container<Actor> container;
        private Table table;

        public Builder defaultSettings(float width, float height, float posX, float posY) {
            container = new Container<>();
            container.setSize(width, height);
            container.setPosition(posX, posY);
            container.fillX();
            //container.setDebug(true);
            return this;
        }

        public Builder addDefaultPadding(float value) {
            table.defaults().pad(value);
            return this;
        }

        public Builder addPadding(float value) {
            table.row().padTop(value);
            return this;
        }

        public Builder addTable() {
            table = new Table();
            table.setFillParent(true);
            return this;
        }

        public Builder addLabel(String text, Skin skin, String style, float scale) {
            Label label = new Label(text, skin);
            if (!style.isEmpty()) {
                label.setStyle(skin.get(style, Label.LabelStyle.class));
            }
            label.setFontScale(scale * Gdx.graphics.getHeight() / defaultHeight);
            table.add(label).fillX();
            return this;
        }

        public Builder addSlider(Slider slider, ChangeListener listener) {
            slider.addListener(listener);
            table.add(slider).growX();
            return this;
        }

        public Builder addTextButton(TextButton button, float scale) {
            button.getLabel().setFontScale(scale * Gdx.graphics.getHeight() / defaultHeight);
            table.add(button).fillX();
            return this;
        }

        public Container<Actor> build() {
            container.setActor(table);
            return container;
        }
    }

    public static TextButton getTextButton(String text, Skin skin, ClickListener listener) {
        TextButton button = new TextButton(text, skin);
        button.addListener(listener);
        return button;
    }

    public static Container<Actor> buildMenuContainer(List<TextButton> buttons, Skin skin) {
        float width = Gdx.graphics.getWidth() * 0.45f;
        float height = Gdx.graphics.getHeight() * 0.95f;

        float titleScale = 2.5f;
        float buttonScale = 1.5f;

        Menu.Builder menuBuilder = new Menu.Builder()
                .defaultSettings(width, height, 0, (Gdx.graphics.getHeight() - height) / 2)
                .addTable()
                .addDefaultPadding(10.0f)
                .addLabel("The Maze", skin, "big", titleScale)
                .addPadding(50.0f);
        for (TextButton button : buttons) {
            menuBuilder
                    .addTextButton(button, buttonScale)
                    .addPadding(0.0f);
        }
        return menuBuilder.build();
    }
}
