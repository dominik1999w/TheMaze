package com.pz.themaze;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import debug.DebugGameApp;

public class DebugDesktopLauncher {

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "The Maze";
        config.width = 960;
        config.height = 540;
        config.x = 50;
        config.y = 50;
        new LwjglApplication(new DebugGameApp(), config);
    }

}
