package time;

import java.util.concurrent.atomic.AtomicBoolean;

public class CustomTimer {
    private AtomicBoolean canceled = new AtomicBoolean(false);

    public void executeAtFixedRate(TimedRunnable runnable, float secondsRate) {
        final long deltaTime = (long) (1e9 * secondsRate);
        long currentTime = System.nanoTime();
        long accumulator = 0;

        while (!canceled.get()) {
            long time = System.nanoTime();
            long frameTime = time - currentTime;
            currentTime = time;

            accumulator += frameTime;

            while (accumulator >= deltaTime) {
                runnable.run(secondsRate);
                accumulator -= deltaTime;
            }
        }
    }

    public void cancel() {
        canceled.set(true);
    }

    @FunctionalInterface
    public interface TimedRunnable {
        void run(float delta);
    }
}
