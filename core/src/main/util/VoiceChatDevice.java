package util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;

public class VoiceChatDevice {

    /**
     * The default sampling rate for audio. 22050.
     */
    private static final int DEFAULT_SAMPLE_RATE = 22050;
    private AudioRecorder recorder;
    private AudioDevice player;
    private int sampleRate = DEFAULT_SAMPLE_RATE; // Default and standard.

    private short[] buffer;

    public VoiceChatDevice() {
        this.recorder = Gdx.audio.newAudioRecorder(this.getSampleRate(), true);
        this.player = Gdx.audio.newAudioDevice(this.getSampleRate(), true);
    }

    public void writeSamples(short[] samples) {
        player.writeSamples(samples, 0, samples.length);
    }

    public short[] readSamples(float hz) {
        int count = (int) (sampleRate / hz);

        if (buffer == null){
            buffer = new short[count];
        }

        recorder.read(buffer, 0, count);
        return buffer;
    }

    public void dispose() {
        player.dispose();
        recorder.dispose();
    }

    /**
     * Gets the audio recording sample rate. Default value is {@link #DEFAULT_SAMPLE_RATE}.
     * @return The current sampling rate. This can be changed at runtime, but is not recommended.
     */
    public int getSampleRate(){
        return this.sampleRate;
    }
}
