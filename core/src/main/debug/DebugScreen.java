package debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.esotericsoftware.kryonet.Connection;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import connection.VoiceNetData;
import entity.bullet.BulletController;
import entity.bullet.BulletHitbox;
import entity.player.Player;
import entity.player.PlayerHitbox;
import entity.player.PlayerInput;
import entity.player.controller.AuthoritativePlayerController;
import entity.player.controller.InputPlayerController;
import map.Map;
import map.MapConfig;
import map.generator.MapGenerator;
import physics.CollisionWorld;
import util.Point2D;
import world.World;

public class DebugScreen extends ScreenAdapter {
    private final DebugGameApp debugGameApp;

    private static final int DEFAULT_SAMPLE_RATE = 22050;
    private AudioRecorder recorder;
    private AudioDevice player;
    private int sampleRate = DEFAULT_SAMPLE_RATE; // Default and standard.
    private float sendRate = 20f;
    private short[] data;
    private float timer;
    private boolean ready = true;

    private final AsyncExecutor asyncExecutor;

    public DebugScreen(DebugGameApp debugGameApp) {
        this.debugGameApp = debugGameApp;
        this.asyncExecutor = new AsyncExecutor(2);
    }

    private void createRecorder() {
        this.recorder = Gdx.audio.newAudioRecorder(sampleRate, true);
    }

    private void createPlayer() {
        this.player = Gdx.audio.newAudioDevice(sampleRate, true);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.2f, .3f, .8f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (this.player == null) this.createPlayer();

        float interval = 1f / sendRate;
        timer += delta;
        if (timer >= interval) {

            if (!ready) {
                timer = interval; // Keep 'on-edge'
                return;
            }
            timer -= interval;

            // Make new thread
            ready = false;
            asyncExecutor.submit(() -> {
                // Need to check if data needs sending. TODO
                int packetSize = (int) (sampleRate / sendRate);
                if (data == null) {
                    data = new short[packetSize];
                }

                // This will block! We need to do this in a separate thread!
                if (this.recorder == null) this.createRecorder();
                this.recorder.read(data, 0, packetSize);

                processAudio(data);

                ready = true;
                return null;
            });
        }
    }

    public void processAudio(short[] samples) {
        asyncExecutor.submit(() -> {
            try {
                player.writeSamples(samples, 0, samples.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public void dispose() {
        this.player.dispose();
        this.player = null;
        this.recorder.dispose();
        this.recorder = null;
    }
}
