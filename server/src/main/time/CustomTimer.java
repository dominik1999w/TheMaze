package time;

public class CustomTimer {
    public static void executeAtFixedRate(TimedRunnable runnable, float secondsRate) {
        final long deltaTime = (long) (1e9 * secondsRate);
        long currentTime = System.nanoTime();
        long accumulator = 0;

        while (true) {
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

    @FunctionalInterface
    public interface TimedRunnable {
        void run(float delta);
    }
}
