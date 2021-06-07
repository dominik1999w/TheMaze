package connection.voice;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import connection.VoiceNetData;

/**
 * A class that is used to send and receive voice chat. Works using KryoNet and LibGDX.
 * See <a href="https://github.com/Epicguru/VoiceChat">the github page</a> for the project and for my other projects.
 * <li>IMPORTANT:
 * You are allowed to use this mini-library in any way you wish. You may use it in any project, commercial or not, 
 * and feel free to edit the source code. Just please note that I put some time and effort into making this, so it would be great if
 * you left this here so that people know that it was me that made this. Thanks. 
 * (Also if you are feeling generous or you appreciate what I have done, it would be great if you put me in the 'credits' section of you game or project.)
 * 
 * @author James Billy, 2017
 *
 */
public class VoiceChatClient implements Disposable{

	private final UUID id;
	private float sendRate = 20f;
	private float timer;
	private boolean ready = true;

	private final AsyncExecutor asyncExecutor;
	
	/**
	 * Creates a new {@link VoiceChatClient} and registers net objects.
	 * @param kryo The {@link Kryo} object that exists in KryoNet Clients and Servers.
	 * <li>See <code>client.getKryo()</code> and <code>server.getKryo()</code>.
	 * @see 
	 */
	public VoiceChatClient(UUID id, Kryo kryo){
		this.registerNetObjects(kryo);
		this.id = id;
		this.asyncExecutor = new AsyncExecutor(1);
	}
	
	/**
	 * Gets the time per second that the audio is sent to the server. This can be changed at runtime, and will work at all values from 3 to 50.
	 * A lower value means that more data will be sent per package, but it will also result in more latency. A higher value means less data per package,
	 * which is generally worse than more, heavy packages, but also gives less latency. A good value for this is between 5 and 10.
	 * @return The current amount of times that audio data is sent per second.
	 */
	public float getSendRate(){
		return this.sendRate;
	}

	protected void registerNetObjects(Kryo kryo){
		kryo.register(short[].class);
		kryo.register(UUID.class);
		kryo.register(VoiceNetData.class);
	}
	
	/**
	 * Makes this chat client process and respond to audio sent from the server. If this message is not called, you will not hear anything
	 * from the server!
	 * @param client The client that audio data will be sent to from the server. Just use the normal client.
	 */
	public void addReceiver(Client client, Consumer<VoiceNetData> voiceSamplesHandler) {
		client.addListener(new Listener(){
			public void received(Connection connection, Object object) {
				// Only read objects of the correct type.
				if (object instanceof VoiceNetData) {
					// Read data
					VoiceNetData message = (VoiceNetData)object;
					voiceSamplesHandler.accept(message);
				}
			}			
		});
	}

	/**
	 * Sends what you are saying to the other connections! This method should be called whenever the client wants to send audio, 
	 * such as when he/she presses a button. This method does not block at all, as recording audio is done in another thread.
	 * The minimum time recorded is equal to:
	 * <code>((SampleRate / SendRate) / SampleRate)</code> in seconds. The maximum time recorded is infinite.
	 * @param client The client to send the data on.
	 * @param delta The time, in seconds, between concurrent calls to this method.
	 * If this method is called 60 times per second, this value should be (1/60). In LibGDX, use <code>Gdx.graphics.getDeltaTime()</code>.
	 */
	public void sendVoice(Client client, float delta, Function<Float, short[]> audioSampler){
		float interval = 1f / this.getSendRate();
		timer += delta;
		if(timer >= interval){
			
			if(!ready){
				timer = interval; // Keep 'on-edge'
				return;
			}
			timer -= interval;
			
			// Make new thread
			ready = false;
			asyncExecutor.submit(() -> {
				// Need to check if data needs sending. TODO
				// Send to server, this will not block but may affect networking...
				client.sendUDP(new VoiceNetData(id, audioSampler.apply(this.getSendRate())));

				ready = true;
				return null;
			});
		}		
	}	
	
	/**
	 * Disposes this voice chat client, which releases all resources but also makes this object unusable. 
	 * Any calls to methods after this will NOT work and will crash.
	 */
	public void dispose(){
	}

	public interface VoiceSamplesHandler {
		void handle(UUID playerID, short[] samples);
	}
}
