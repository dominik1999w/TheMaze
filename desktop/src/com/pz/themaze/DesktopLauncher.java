package com.pz.themaze;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import game.GameApp;

public class DesktopLauncher {

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "The Maze";
        config.width = 960;
        config.height = 540;
        new LwjglApplication(new GameApp(), config);
    }

}
