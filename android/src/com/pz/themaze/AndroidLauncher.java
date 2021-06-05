package com.pz.themaze;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import game.GameApp;
import game.Permissions;

public class AndroidLauncher extends AndroidApplication implements Permissions {
    private static final int MY_PERMISSIONS_RECORD_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        initialize(new GameApp(this), config);
    }


    @Override
    public boolean isAudioPermissionEnabled() {
        int currentSDKVersion = Build.VERSION.SDK_INT;
        if (currentSDKVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            int permissionResult = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
            return permissionResult == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    @Override
    public void requestAudioPermission() {
        int currentSDKVersion = Build.VERSION.SDK_INT;
        if (currentSDKVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            requestPermissions(new String[]{ Manifest.permission.RECORD_AUDIO }, MY_PERMISSIONS_RECORD_AUDIO);
        }
    }
}
