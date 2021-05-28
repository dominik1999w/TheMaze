package time;

public class Timer {

    public Timer() {
    }

    private volatile boolean run = true;

    public void executeAtFixedRate(TimedRunnable runnable, float secondsRate) {
        final long deltaTime = (long) (1e9 * secondsRate);
        long currentTime = System.nanoTime();
        long accumulator = 0;

        while (run) {
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
        run = false;
    }

    @FunctionalInterface
    public interface TimedRunnable {
        void run(float delta);
    }
}
